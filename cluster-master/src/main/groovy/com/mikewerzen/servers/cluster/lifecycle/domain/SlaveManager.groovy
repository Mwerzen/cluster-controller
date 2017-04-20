package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException
import com.sun.javafx.collections.NonIterableChange.GenericAddRemoveChange

public class SlaveManager
{

	private Set<Slave> slavesInCluster = new HashSet<Slave>();

	public Slave findSlaveForName(String name)
	{
		return slavesInCluster.find{slave -> slave.slaveName.equals(name)};
	}

	public void registerSlave(Slave slave)
	{
		slavesInCluster.add(slave);
	}

	public int getNumberOfSlavesRunningSameVersionOfDeployment(Deployment deployment)
	{
		return slavesInCluster.findAll{slave -> slave.isRunningSameVersionOfDeployment(deployment)}.size();
	}

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
			throw new ClusterIntegrityException("There are no eligible slaves for deployment of $deployment.applicationName. The cluster is overwhelmed.");
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

	public void undeployFromCluster(Deployment deployment, boolean allVersions)
	{
		if(allVersions)
			undeployAllVersionsFromCluster(deployment);
		undeployThisVersionFromCluster(deployment);
	}

	public void undeployThisVersionFromCluster(Deployment deployment)
	{
		slavesInCluster.each{ slave -> if (slave.isRunningSameVersionOfDeployment(deployment)) slave.removeDeployment(deployment)};
	}

	public void undeployAllVersionsFromCluster(Deployment deployment)
	{
		slavesInCluster.each{ slave -> slave.removeDeployment(deployment)};
	}

	public void rebootSlave(String name)
	{
		rebootSlave (findSlaveForName(name));
	}

	public void rebootSlave(Slave slave)
	{
		slavesInCluster.remove{slave.reboot()};
	}

	public void shutdownSlave(String name)
	{
		slavesInCluster.remove {findSlaveForName(name).shutdown()};
	}

	public void shutdownSlave(Slave slave)
	{
		slavesInCluster.remove {slave.shutdown()};
	}

	public List shutdownDeadSlaves()
	{
		List deadSlaves = slavesInCluster.findAll{slave -> slave.isSlaveDead()};
		deadSlaves.each{shutdownSlave};
		slavesInCluster.removeAll(deadSlaves);
		return deadSlaves;
	}
}
