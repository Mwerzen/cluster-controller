package com.mikewerzen.servers.cluster.lifecycle.domain;

public class DeploymentManager
{

	private Set<Deployment> deployments = new HashSet<Deployment>();

	public boolean deployApplication(Deployment deployment, boolean keepExistingDeploymentsOfApplication)
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

	public boolean isApplicationDeployed(Deployment deployment)
	{
	}
}
