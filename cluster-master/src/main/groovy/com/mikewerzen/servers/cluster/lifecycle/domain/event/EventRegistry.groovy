package com.mikewerzen.servers.cluster.lifecycle.domain.event;

import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave

public class EventRegistry
{
	private static EventRegistry instance = new EventRegistry();

	List deploymentEvents = new ArrayList<ClusterEvent>();
	List undeploymentEvents = new ArrayList<ClusterEvent>();
	List rebootEvents = new ArrayList<ClusterEvent>();
	List shutdownEvents = new ArrayList<ClusterEvent>();

	private EventRegistry()
	{
	}

	public static EventRegistry getInstance()
	{
		return instance;
	}

	public synchronized void addDeploymentEvent(Slave slave, Deployment deployment)
	{
		deploymentEvents.add(new ClusterEvent(slave, deployment));
	}

	public synchronized void addUndeploymentEvent(Slave slave, Deployment deployment)
	{
		undeploymentEvents.add(new ClusterEvent(slave, deployment));
	}

	public synchronized void addRebootEvent(Slave slave, Deployment deployment)
	{
		rebootEvents.add(new ClusterEvent(slave, deployment));
	}

	public synchronized void addShutdownEvent(Slave slave, Deployment deployment)
	{
		shutdownEvents.add(new ClusterEvent(slave, deployment));
	}

	public synchronized List<ClusterEvent> getAndClearDeploymentEvents()
	{
		List ret = deploymentEvents.clone();
		deploymentEvents.clear();
		return ret;
	}

	public synchronized List<ClusterEvent> getAndClearUndeploymentEvents()
	{
		List ret = undeploymentEvents.clone();
		undeploymentEvents.clear();
		return ret;
	}

	public synchronized List<ClusterEvent> getAndClearRebootEvents()
	{
		List ret = rebootEvents.clone();
		rebootEvents.clear();
		return ret;
	}

	public synchronized List<ClusterEvent> getAndClearShutdownEvents()
	{
		List ret = shutdownEvents.clone();
		shutdownEvents.clear();
		return ret;
	}
}
