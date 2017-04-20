package com.mikewerzen.servers.cluster.lifecycle.domain;

public class DeploymentManager
{
	private Set<Deployment> deployments = new HashSet<Deployment>();

	public boolean addDeployment(Deployment deployment, boolean keepExistingDeploymentsOfApplication)
	{
		if (!keepExistingDeploymentsOfApplication)
		{
			deployments.removeIf { dep -> deployment.isSameApplication(dep) };
			deployments.add(deployment);
		}
		else
		{
			if( deployments.contains(deployment))
			{
				def oldDeployment = deployments.find { dep -> deployment.equals(deployment)};
				oldDeployment.replicationFactor += deployment.replicationFactor;
			}
			else
			{
				deployments.add(deployment);
			}
		}
	}

	public Deployment findDeployment(String name)
	{
		return deployments.find { dep -> name.equals(dep.applicationName)};
	}

	public Deployment findDeployment(String name, String version)
	{
		return deployments.find { dep -> name.equals(dep.applicationName) && version.equals(dep.applicationVersion)};
	}

	public boolean isApplicationDeployed(Deployment deployment, boolean anyVersion)
	{
		return deployments.find { dep -> anyVersion ? deployment.isSameApplication(dep) : deployment.isSameVersionOfApplication(dep)} != null;
	}

	public boolean removeDeployment(Deployment deployment, boolean removeAllVersions)
	{
		deployments.removeIf { dep ->  removeAllVersions ? deployment.isSameApplication(dep) : deployment.isSameVersionOfApplication(dep)};
	}
}
