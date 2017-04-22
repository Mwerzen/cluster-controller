package com.mikewerzen.servers.cluster.lifecycle.domain.event;

public class InboundClusterEvent
{
	String name;
	double load;
	String app;
	String version;

	public InboundClusterEvent(String name, double load)
	{
		super();
		this.name = name;
		this.load = load;
	}

	public InboundClusterEvent(String name, String app, String version)
	{
		super();
		this.name = name;
		this.app = app;
		this.version = version;
	}

	@Override
	public String toString()
	{
		return "InboundClusterEvent [name=" + name + ", load=" + load + ", app=" + app + ", version=" + version + "]";
	}

}
