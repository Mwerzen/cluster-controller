package com.mikewerzen.servers.cluster.lifecycle.domain;

import java.util.Date
import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.DeploymentManager
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveManager
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry

class ClusterTestHelper
{
	public static void resetRegistry()
	{
		EventRegistry.getInstance().getAndClearDeploymentEvents();
		EventRegistry.getInstance().getAndClearUndeploymentEvents();
		EventRegistry.getInstance().getAndClearRebootEvents();
		EventRegistry.getInstance().getAndClearShutdownEvents();
	}

	public static ClusterController buildClusterController(int numberOfSlaves, int...replicationFactors)
	{
		def cc = new ClusterController();
		cc.deploymentManager = buildDeploymentManager(replicationFactors);
		cc.slaveManager = buildSlaveManager(numberOfSlaves);

		if(replicationFactors)
			cc.deploymentManager.deployments.each {d -> cc.slaveManager.deployToCluster(d)};

		resetRegistry()

		return cc;
	}

	public static SlaveManager buildSlaveManager(int numberOfSlaves)
	{
		def sm = new SlaveManager();

		if(numberOfSlaves)
			0.upto(numberOfSlaves - 1, {sm.registerSlave(buildSlave("Slave" + it))});

		resetRegistry()

		return sm;
	}

	public static DeploymentManager buildDeploymentManager(int numberOfDeployments)
	{
		def dm = new DeploymentManager();

		if(numberOfDeployments)
			0.upto(numberOfDeployments - 1, {dm.addDeployment(buildDeployment("App" + it), true)});

		resetRegistry()

		return dm;
	}

	public static DeploymentManager buildDeploymentManager(int...replicationFactors)
	{
		def dm = new DeploymentManager();

		def numberOfDeployments = (replicationFactors) ? replicationFactors.size() : 0;

		if(numberOfDeployments)
			0.upto(numberOfDeployments - 1, {dm.addDeployment(buildDeployment("App" + it, "1", replicationFactors[it]), true)});

		resetRegistry()

		return dm;
	}

	private static Slave buildSlave(String name = "Slave", double load = 0, long lastCheckIn = System.currentTimeMillis())
	{
		Slave slave = new Slave();
		slave.slaveName = name;
		slave.loadOnSlave = load;
		slave.lastCheckInMillis = lastCheckIn;
		return slave;
	}

	private static Deployment buildDeployment(String name = "App", String version = "1", int replicationFactor = 1)
	{
		Deployment deployment = new Deployment();
		deployment.applicationName = name;
		deployment.applicationVersion = version;
		deployment.deploymentCommands = "./deploy.sh " + name + "-" + version;
		deployment.replicationFactor = replicationFactor;
		return deployment;
	}
}
