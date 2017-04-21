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
		if(slave)
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

	public List<Slave> findOptimalSlavesForDeployment(Deployment deployment)
	{
		findOptimalSlavesForDeploymentWithModifier(deployment, 0);
	}

	public List<Slave> findOptimalSlavesForDeploymentWithModifier(Deployment deployment, int modifier)
	{
		int numberOfSlavesNeeded = deployment.replicationFactor - getNumberOfSlavesRunningSameVersionOfDeployment(deployment) + modifier;
		return findOptimalSlavesForDeployment(deployment, numberOfSlavesNeeded);
	}

	public List<Slave> findOptimalSlavesForDeployment(Deployment deployment, int numberOfSlaves)
	{
		if (numberOfSlaves < 1)
		{
			return null;
		}

		List<Slave> slavesNotRunningAnyVersionOfDeployment = slavesInCluster.stream().filter({slave -> !slave.isRunningAnyVersionOfDeployment(deployment)}).collect();

		if (slavesNotRunningAnyVersionOfDeployment.size() == 0)
		{
			throw new ClusterIntegrityException("There are no eligible slaves for deployment of $deployment.applicationName. The cluster is overwhelmed.");
		}

		List<Slave> sortedSlaves = slavesNotRunningAnyVersionOfDeployment.toSorted{a, b -> a.loadOnSlave <=> b.loadOnSlave};

		if(sortedSlaves.size() > numberOfSlaves)
		{
			return sortedSlaves.subList(0, numberOfSlaves);
		}

		return sortedSlaves;
	}

	public List<Slave> reassignDeploymentToNewSlave(Deployment deployment, Slave slave)
	{
		//Note, we are finding an optimal slave for deployment BEFORE we remove deployment from the slave;
		//this prevents us from redeploying to the failed slave.
		List<Slave> slaves = findOptimalSlavesForDeployment(deployment, 1);
		slaves.each {it.addDeployment(deployment)};
		slave.removeDeployment(deployment);
		return slaves;

	}

	public List<Slave> deployToCluster(Deployment deployment)
	{
		List<Slave> slaves = findOptimalSlavesForDeployment(deployment);
		slaves.each{slave -> slave.addDeployment(deployment)};
		return slaves;
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
		shutdownSlave(findSlaveForName(name));
	}

	public void shutdownSlave(Slave slave)
	{
		slave.shutdown()
		slavesInCluster.remove {slave};
	}

	public void rebalanceSlaves(Set<Deployment> deployments)
	{
		for (deployment in deployments)
		{
			try
			{
				deployToCluster(deployment);
			}
			catch (ClusterIntegrityException exception)
			{
				continue;
			}
		}


	}

	public Set<Slave> shutdownDeadSlaves()
	{
		Set<Slave> deadSlaves = slavesInCluster.findAll{slave -> slave.isSlaveDead()};
		deadSlaves.each{shutdownSlave(it)};
		slavesInCluster.removeAll(deadSlaves);
		return deadSlaves;
	}

	@Override
	public String toString()
	{
		return "SlaveManager [slavesInCluster=" + slavesInCluster + "]";
	}
}
