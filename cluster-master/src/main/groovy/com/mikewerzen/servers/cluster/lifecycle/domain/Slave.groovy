package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry
import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException

import sun.security.jca.GetInstance

import java.util.Date

public class Slave
{
	private static EventRegistry eventRegistry = EventRegistry.getInstance();

	String slaveName;
	double loadOnSlave;
	long lastCheckInMillis;
	Set<Deployment> deploymentsRunning = new HashSet<Deployment>();

	public Slave()
	{
	}

	public Slave(String slaveName, double loadOnSlave, Deployment...deployments)
	{
		this.slaveName = slaveName;
		this.loadOnSlave = loadOnSlave;
		deploymentsRunning.addAll(deployments);
		lastCheckInMillis = System.currentTimeMillis();
	}

	public String refreshSlave(double load)
	{
		this.loadOnSlave = load;
		lastCheckInMillis = System.currentTimeMillis();
	}

	public boolean isRunningSameVersionOfDeployment(Deployment deployment)
	{
		return deploymentsRunning.find {dep -> deployment.isSameVersionOfApplication dep} != null;
	}

	public boolean isRunningAnyVersionOfDeployment(Deployment deployment)
	{
		return deploymentsRunning.find {dep -> deployment.isSameApplication dep} != null;
	}


	public Deployment addDeployment(Deployment deployment)
	{
		if (isRunningAnyVersionOfDeployment(deployment))
		{
			throw new ClusterIntegrityException("Slave: " + this + " is already running a version of this deployment! Undeploy first.");
		}

		deploymentsRunning.add(deployment);
		eventRegistry.addDeploymentEvent(this, deployment);
		return deployment;
	}

	public Deployment removeDeployment(Deployment deployment)
	{
		if(isRunningAnyVersionOfDeployment(deployment))
		{
			deploymentsRunning.removeIf {dep -> deployment.isSameApplication(dep) };
			eventRegistry.addUndeploymentEvent(this, deployment);
			return deployment;
		}
		return null;
	}

	public Slave reboot()
	{
		deploymentsRunning.each{removeDeployment};
		eventRegistry.addRebootEvent(this, null);
		return this;
	}

	public Slave shutdown()
	{
		deploymentsRunning.each{removeDeployment};
		eventRegistry.addShutdownEvent(this, null);
		return this;
	}

	public boolean isSlaveDead()
	{
		return lastCheckInMillis < (System.currentTimeMillis() - (60 * 1000L));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((slaveName == null) ? 0 : slaveName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slave other = (Slave) obj;
		if (slaveName == null)
		{
			if (other.slaveName != null)
				return false;
		}
		else if (!slaveName.equals(other.slaveName))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Slave [slaveName=" + slaveName + ", deploymentsRunning=" + deploymentsRunning + "]";
	}
}
