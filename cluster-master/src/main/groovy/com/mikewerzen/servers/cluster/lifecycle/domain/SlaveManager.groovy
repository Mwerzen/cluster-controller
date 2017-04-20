package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException

public class SlaveManager
{
	private Set slavesInCluster = new HashSet<Slave>();


	public boolean isClusterCurrentlyRunningAnyVersionOfDeployment(Deployment deployment)
	{
		return slavesInCluster.find{slave -> slave.isRunningAnyVersionOfDeployment deployment} != null;
	}

	public boolean isClusterCurrentlyRunningSameVersionOfDeployment(Deployment deployment)
	{
		return slavesInCluster.find{slave -> slave.isRunningSameVersionOfDeployment deployment} != null;
	}

	public List findOptimalSlavesForDeployment(Deployment deployment)
	{
		List<Slave> slavesNotRunningAnyVersionOfDeployment = slavesInCluster.stream().filter({slave -> !slave.isRunningAnyVersionOfDeployment(deployment)}).collect();

		if (slavesNotRunningAnyVersionOfDeployment.size() == 0)
		{
			throw new ClusterIntegrityException("There are no eligible slaves for deployment. The cluster is overwhelmed.");
		}

		List<Slave> sortedSlaves = slavesNotRunningAnyVersionOfDeployment.toSorted{a, b -> a.loadOnSlave <=> b.loadOnSlave};

		if(sortedSlaves.size() > deployment.replicationFactor)
		{
			return sortedSlaves.subList(0, deployment.replicationFactor);
		}

		return sortedSlaves;
	}

	public List deployToCluster(Deployment deployment)
	{
		return findOptimalSlavesForDeployment(deployment).stream().map({slave -> slave.addDeployment(deployment)}).collect();
	}

	public void undeployThisVersionFromCluster(Deployment deployment)
	{
		slavesInCluster.each{ slave -> if (slave.isRunningSameVersionOfDeployment(deployment)) slave.removeDeployment(deployment)};
	}

	public void undeployAllVersionsFromCluster(Deployment deployment)
	{
		slavesInCluster.each{ slave -> slave.removeDeployment(deployment)};
	}
}
