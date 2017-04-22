package com.mikewerzen.servers.cluster.lifecycle.domain.event;

public class InboundClusterEvent
{
	String name;
	String load;
	String app;
	String version;

	public InboundClusterEvent(String name, String load)
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

}
