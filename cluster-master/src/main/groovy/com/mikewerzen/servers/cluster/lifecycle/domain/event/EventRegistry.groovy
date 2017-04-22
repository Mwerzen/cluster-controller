package com.mikewerzen.servers.cluster.lifecycle.domain.event;

import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave

public class EventRegistry
{
	private static EventRegistry instance = new EventRegistry();

	List statusEvents = new ArrayList<InboundClusterEvent>();
	List finishedEvents = new ArrayList<InboundClusterEvent>();
	List failedEvents = new ArrayList<InboundClusterEvent>();

	List deploymentEvents = new ArrayList<OutboundClusterEvent>();
	List undeploymentEvents = new ArrayList<OutboundClusterEvent>();
	List rebootEvents = new ArrayList<OutboundClusterEvent>();
	List shutdownEvents = new ArrayList<OutboundClusterEvent>();

	private EventRegistry()
	{
	}

	public static EventRegistry getInstance()
	{
		return instance;
	}

	public synchronized void addStatusEvent(String name, double load)
	{
		println("Recieved status from $name with load $load");
		statusEvents.add(new InboundClusterEvent(name, load));
	}

	public synchronized void addFinishedEvent(String name, String app, String version)
	{
		println("Recieved finished event from $name for $app version $version");
		finishedEvents.add(new InboundClusterEvent(name, app, version));
	}

	public synchronized void addFailedEvent(String name, String app, String version)
	{
		println("Recieved failed event from $name for $app version $version");
		failedEvents.add(new InboundClusterEvent(name, app, version));
	}

	public synchronized void addDeploymentEvent(Slave slave, Deployment deployment)
	{
		println("Deploying $deployment to $slave");
		deploymentEvents.add(new OutboundClusterEvent(slave, deployment));
	}

	public synchronized void addUndeploymentEvent(Slave slave, Deployment deployment)
	{
		println("Undeploying $deployment from $slave");
		undeploymentEvents.add(new OutboundClusterEvent(slave, deployment));
	}

	public synchronized void addRebootEvent(Slave slave, Deployment deployment)
	{
		println("Rebooting $slave");
		rebootEvents.add(new OutboundClusterEvent(slave, deployment));
	}

	public synchronized void addShutdownEvent(Slave slave, Deployment deployment)
	{
		println("Shutting down $slave");
		shutdownEvents.add(new OutboundClusterEvent(slave, deployment));
	}

	public synchronized List<InboundClusterEvent> getAndClearStatusEvents()
	{
		List ret = statusEvents.clone();
		statusEvents.clear();
		return ret;
	}

	public synchronized List<InboundClusterEvent> getAndClearFinishedEvents()
	{
		List ret = finishedEvents.clone();
		finishedEvents.clear();
		return ret;
	}

	public synchronized List<InboundClusterEvent> getAndClearFailedEvents()
	{
		List ret = failedEvents.clone();
		failedEvents.clear();
		return ret;
	}

	public synchronized List<OutboundClusterEvent> getAndClearDeploymentEvents()
	{
		List ret = deploymentEvents.clone();
		deploymentEvents.clear();
		return ret;
	}

	public synchronized List<OutboundClusterEvent> getAndClearUndeploymentEvents()
	{
		List ret = undeploymentEvents.clone();
		undeploymentEvents.clear();
		return ret;
	}

	public synchronized List<OutboundClusterEvent> getAndClearRebootEvents()
	{
		List ret = rebootEvents.clone();
		rebootEvents.clear();
		return ret;
	}

	public synchronized List<OutboundClusterEvent> getAndClearShutdownEvents()
	{
		List ret = shutdownEvents.clone();
		shutdownEvents.clear();
		return ret;
	}
}
