package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException

public class ClusterController
{

	DeploymentManager deploymentManager;

	SlaveManager slaveManager;


	public List deploy(Deployment deployment)
	{
		def slavesDeployedTo = new ArrayList<Deployment>();
		try
		{
			slavesDeployedTo = slaveManager.deployToCluster(deployment);
			deploymentManager.addDeployment(deployment, false);
		}
		catch (ClusterIntegrityException e)
		{
			println(e);
		}

		return slavesDeployedTo;
	}
}
