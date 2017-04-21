package com.mikewerzen.servers.cluster.lifecycle.domain;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException

@Component
protected class ClusterController
{

	@Autowired
	DeploymentManager deploymentManager;

	@Autowired
	SlaveManager slaveManager;



	@Scheduled(fixedDelay=10000L)
	public void executeStep()
	{
		
		
		
		
	}


	protected Slave findSlave(String name)
	{
		return slaveManager.findSlaveForName(name);
	}

	protected Map<Deployment, List<Slave>> getDeploymentsToSlaves()
	{
		Map<Deployment, List<Slave>> depsToSlaves = new HashMap<Deployment, List<Slave>>();
		deploymentManager.deployments.each {deployment -> depsToSlaves.put(deployment, slaveManager.slavesInCluster.findAll { slave -> slave.isRunningSameVersionOfDeployment(deployment)})};
		return depsToSlaves;
	}

	protected Map<Slave, List<Deployment>> getSlavesToDeployments()
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

	protected List<Slave> deploy(Deployment deployment, boolean keepOldVersions = false)
	{
		def slavesDeployedTo = new ArrayList<Deployment>();
		try
		{
			if(!keepOldVersions)
				slaveManager.undeployAllVersionsFromCluster(deployment);

			slavesDeployedTo = slaveManager.deployToCluster(deployment);
			deploymentManager.addDeployment(deployment, keepOldVersions);
		}
		catch (ClusterIntegrityException e)
		{
			println(e);
		}

		return slavesDeployedTo;
	}

	protected List<Slave> handleFailedDeployment(Deployment deployment, Slave slave)
	{
		return slaveManager.reassignDeploymentToNewSlave(deployment, slave);
	}

	protected void undeploy(Deployment deployment, boolean allVersions)
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
	}

	protected void rebalanceCluster()
	{
		slaveManager.shutdownDeadSlaves();
		deploymentManager.deployments.each{dep -> slaveManager.deployToCluster(dep)};
	}
}
