package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException

public class ClusterController
{

	DeploymentManager deploymentManager;

	SlaveManager slaveManager;

	public Slave findSlave(String name)
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


	public Deployment findDeployment(String name, String version)
	{
		if (version)
			return deploymentManager.findDeployment(name, version);
		return deploymentManager.findDeployment(name);
	}

	public List<Slave> deploy(Deployment deployment, boolean keepOldVersions = false)
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

	public List<Slave> handleFailedDeployment(Deployment deployment, Slave slave)
	{
		return slaveManager.reassignDeploymentToNewSlave(deployment, slave);
	}

	public void undeploy(Deployment deployment, boolean allVersions)
	{
		slaveManager.undeployFromCluster(deployment, allVersions);
		deploymentManager.removeDeployment(deployment, allVersions);
	}

	public void refreshSlave(String name, double load)
	{
		Slave slave = findSlave(name);
		if(slave)
		{	
			slave.lastCheckInMillis = System.currentTimeMillis();
			slave.loadOnSlave = load;
		}
	}

	public void rebalanceCluster()
	{
		slaveManager.shutdownDeadSlaves();
		deploymentManager.deployments.each{dep -> slaveManager.deployToCluster(dep)};
	}
}
