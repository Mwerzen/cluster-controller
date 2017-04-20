package com.mikewerzen.servers.cluster.lifecycle.domain.event;

import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave

public class ClusterEvent
{
	public Slave slave;
	public Deployment deployment;


	public ClusterEvent(Slave slave, Deployment deployment)
	{
		super();
		this.slave = slave;
		this.deployment = deployment;
	}

	@Override
	public String toString()
	{
		return "ClusterEvent [slave=" + slave.slaveName + ", deployment=" + deployment.applicationName + "]";
	}
}
