package com.mikewerzen.servers.cluster.lifecycle.domain;

public class DeploymentManager
{
	private Set<Deployment> deployments = new HashSet<Deployment>();

	public boolean addDeployment(Deployment deployment, boolean keepExistingDeploymentsOfApplication)
	{
		if (!keepExistingDeploymentsOfApplication)
		{
			deployments.removeIf { dep -> deployment.isSameApplication(deployment) };
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

	public boolean isApplicationDeployed(Deployment deployment, boolean anyVersion)
	{
		return deployments.find { dep -> anyVersion ? deployment.isSameApplication(dep) : deployment.isSameVersionOfApplication(dep)} != null;
	}
	
	public boolean removeDeployment(Deployment deployment, boolean removeAllVersions)
	{
		deployments.removeIf { dep ->  removeAllVersions ? deployment.isSameApplication(deployment) : deployment.isSameVersionOfApplication(dep)};
	}
}
