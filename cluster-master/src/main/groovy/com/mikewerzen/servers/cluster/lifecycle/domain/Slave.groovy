package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException
import java.util.Date

public class Slave
{
	String slaveName;
	double loadOnSlave;
	Date lastCheckIn;
	Set<Deployment> deploymentsRunning = new HashSet<Deployment>();

	public Slave(String slaveName, double loadOnSlave, Deployment...deployments)
	{
		this.slaveName = slaveName;
		this.loadOnSlave = loadOnSlave;
		deploymentsRunning.addAll(deployments);
		lastCheckIn = new Date();
	}

	public String refreshSlave(double load)
	{
		this.loadOnSlave = load;
		lastCheckIn = new Date();
	}

	public boolean isRunningSameVersionOfDeployment(Deployment deployment)
	{
		return deploymentsRunning.find {deployment.isSameVersionOfApplication} != null;
	}

	public boolean isRunningAnyVersionOfDeployment(Deployment deployment)
	{
		return deploymentsRunning.find {deployment.isSameApplication} != null;
	}


	public void addDeployment(Deployment deployment)
	{
		if (isRunningAnyVersionOfDeployment(deployment))
		{
			throw new ClusterIntegrityException("Slave: " + slave + " is already running a version of this deployment! Undeploy first.");
		}

		deploymentsRunning.add(deployment);
	}

	public void removeDeployment(Deployment deployment)
	{
		deploymentsRunning.removeIf {deployment.isSameApplication };
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
}
