package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.event.*;
import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
public class ClusterController
{

	@Autowired
	DeploymentManager deploymentManager;

	@Autowired
	SlaveManager slaveManager;

	@Autowired
	EventPoller eventPoller;

	EventRegistry registry = EventRegistry.getInstance();


	@Scheduled(fixedDelay=10000L)
	public void executeStep()
	{
		eventPoller.pollInboundEvents();
		registry.getAndClearStatusEvents().each {event -> println("Slave Refreshed: $event"); refreshSlave(event.name, event.load) };
		registry.getAndClearFailedEvents().each {event -> handleFailedDeployment(findDeployment(event.app, event.version), findSlave(event.name))};
		registry.getAndClearFinishedEvents();
		rebalanceCluster();
		eventPoller.pollOutboundEvents();
		println(this)
	}


	protected Slave findSlave(String name)
	{
		return slaveManager.findSlaveForName(name);
	}

	public Map<Deployment, List<Slave>> getDeploymentsToSlaves()
	{
		Map<Deployment, List<Slave>> depsToSlaves = new HashMap<Deployment, List<Slave>>();
		deploymentManager.deployments.each {deployment -> depsToSlaves.put(deployment, slaveManager.slavesInCluster.findAll { slave -> slave.isRunningSameVersionOfDeployment(deployment)})};
		return depsToSlaves;
	}

	public Map<Slave, List<Deployment>> getSlavesToDeployments()
	{
		Map<Slave, List<Deployment>> slavesToDeps = new HashMap<Deployment, List<Slave>>();
		slaveManager.slavesInCluster.each {slave -> slavesToDeps.put(slave, slave.deploymentsRunning)};
		return slavesToDeps;
	}


	protected Deployment findDeployment(String name, String version)
	{
		if (version)
			return deploymentManager.findDeployment(name, version);
		return deploymentManager.findDeployment(name);
	}

	public List<Slave> deploy(Deployment deployment, boolean keepOldVersions = false)
	{
		def slavesDeployedTo = new ArrayList<Deployment>();

		if(!keepOldVersions)
			slaveManager.undeployAllVersionsFromCluster(deployment);

		slavesDeployedTo = slaveManager.deployToCluster(deployment);
		deploymentManager.addDeployment(deployment, keepOldVersions);


		return slavesDeployedTo;
	}

	protected List<Slave> handleFailedDeployment(Deployment deployment, Slave slave)
	{
		return slaveManager.reassignDeploymentToNewSlave(deployment, slave);
	}

	public void undeploy(Deployment deployment, boolean allVersions)
	{
		slaveManager.undeployFromCluster(deployment, allVersions);
		deploymentManager.removeDeployment(deployment, allVersions);
	}

	protected void refreshSlave(String name, double load)
	{
		Slave slave = findSlave(name);
		if(slave)
		{
			slave.lastCheckInMillis = System.currentTimeMillis();
			slave.loadOnSlave = load;
		}
		else
		{
			slave = new Slave();
			slave.slaveName = name;
			slave.loadOnSlave = load;
			slaveManager.registerSlave(slave);
		}
	}

	public void rebootCluster()
	{
		Set<Slave> allSlaves = slaveManager.slavesInCluster.clone();
		allSlaves.each{slave -> slaveManager.rebootSlave(slave)};
	}

	public void terminateCluster()
	{
		Set<Slave> allSlaves = slaveManager.slavesInCluster.clone();
		allSlaves.each{slave -> slaveManager.shutdownSlave(slave)};
	}

	protected void rebalanceCluster()
	{
		slaveManager.shutdownDeadSlaves();
		
		slaveManager.rebootInconsistentSlaves();

		deploymentManager.deployments.each
		{dep ->
			try
			{
				if(slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(dep) != dep.getReplicationFactor())
					slaveManager.deployToCluster(dep)
			}
			catch (ClusterIntegrityException e)
			{
				println("Could not balance deployment: $dep")
			}
		};
	}


	@Override
	public String toString()
	{
		return "ClusterController [\n\tdeploymentManager=" + deploymentManager + ", \n\tslaveManager=" + slaveManager + "]";
	}
}
