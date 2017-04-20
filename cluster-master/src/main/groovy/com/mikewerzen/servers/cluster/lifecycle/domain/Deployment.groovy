package com.mikewerzen.servers.cluster.lifecycle.domain

class Deployment
{
	String applicationName;
	String applicationVersion;
	String deploymentCommands;
	int replicationFactor;


	public boolean isSameApplication(Deployment other)
	{
		return other && applicationName && other.applicationName && applicationName.equals(other.applicationName);
	}

	private boolean isSameVersion(Deployment other)
	{
		return other && applicationVersion && other.applicationVersion && applicationVersion.equals(other.applicationVersion);
	}

	public boolean isSameVersionOfApplication(Deployment other)
	{
		return isSameApplication(other) && isSameVersion(other);
	}

	private boolean hasSameDeploymentCommands(Deployment other)
	{
		return deploymentCommands && other.deploymentCommands && deploymentCommands.equals(other.deploymentCommands);
	}

	public boolean isSameDeployment(Deployment other)
	{
		return isSameApplication(other) && isSameVersion(other) && hasSameDeploymentCommands(other);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationName == null) ? 0 : applicationName.hashCode());
		result = prime * result + ((applicationVersion == null) ? 0 : applicationVersion.hashCode());
		result = prime * result + ((deploymentCommands == null) ? 0 : deploymentCommands.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!obj)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Deployment other = (Deployment) obj;
		if (applicationName == null)
		{
			if (other.applicationName != null)
				return false;
		}
		else if (!applicationName.equals(other.applicationName))
			return false;
		if (applicationVersion == null)
		{
			if (other.applicationVersion != null)
				return false;
		}
		else if (!applicationVersion.equals(other.applicationVersion))
			return false;
		if (deploymentCommands == null)
		{
			if (other.deploymentCommands != null)
				return false;
		}
		else if (!deploymentCommands.equals(other.deploymentCommands))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Deployment [applicationName=" + applicationName + ", applicationVersion=" + applicationVersion + "]";
	}
}
