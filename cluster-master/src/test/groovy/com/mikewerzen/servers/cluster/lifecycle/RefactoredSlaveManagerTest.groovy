package com.mikewerzen.servers.cluster.lifecycle;

import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.DeploymentManager
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveManager
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

public class RefactoredSlaveManagerTest
{
	ClusterController controller;
	EventRegistry registry = EventRegistry.getInstance();

	@Before
	public void setup()
	{
		controller = new ClusterController();
		controller.deploymentManager = new DeploymentManager();
		controller.slaveManager = new SlaveManager();

		registry.getAndClearDeploymentEvents();
		registry.getAndClearUndeploymentEvents();
		registry.getAndClearRebootEvents();
		registry.getAndClearShutdownEvents();
	}

	@Test
	public void test_AddDeployment_NoServersInCluster()
	{
		Deployment deployment = buildDeployment();
		def slavesDeployedTo = controller.deploy(deployment);

		assertEquals(0, registry.getAndClearDeploymentEvents().size());
		assertEquals(0, controller.getDeploymentManager().deployments.size());
		assertEquals(0, slavesDeployedTo.size());
		assertFalse(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
	}

	@Test
	public void test_AddDeployment_SingleServerInCluster()
	{
		Slave slave = buildSlave();
		controller.getSlaveManager().registerSlave(slave);

		Deployment deployment = buildDeployment();
		def slavesDeployedTo = controller.deploy(deployment);

		assertEquals(1, registry.getAndClearDeploymentEvents().size());
		assertEquals(1, controller.getDeploymentManager().deployments.size());
		assertEquals(1, slavesDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deployment));
	}

	@Test
	public void test_AddDeploymentWithReplicationFactor2_ThreeServersInCluster()
	{
		controller.getSlaveManager().registerSlave(buildSlave("Slave1"));
		controller.getSlaveManager().registerSlave(buildSlave("Slave2"));
		controller.getSlaveManager().registerSlave(buildSlave("Slave3"));

		Deployment deployment = buildDeployment();
		deployment.setReplicationFactor(2);
		def slavesDeployedTo = controller.deploy(deployment);

		assertEquals(2, registry.getAndClearDeploymentEvents().size());
		assertEquals(1, controller.getDeploymentManager().deployments.size());
		assertEquals(2, slavesDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deployment));
	}

	@Test
	public void test_AddDeploymentWithReplicationFactor4_ThreeServersInCluster()
	{
		controller.getSlaveManager().registerSlave(buildSlave("Slave1"));
		controller.getSlaveManager().registerSlave(buildSlave("Slave2"));
		controller.getSlaveManager().registerSlave(buildSlave("Slave3"));

		Deployment deploymentA = buildDeployment("AppA");
		deploymentA.setReplicationFactor(4);
		def slavesADeployedTo = controller.deploy(deploymentA);
		Deployment deploymentB = buildDeployment("AppB", "2", 2);
		def slavesBDeployedTo = controller.deploy(deploymentB);

		assertEquals(5, registry.getAndClearDeploymentEvents().size());
		assertEquals(2, controller.getDeploymentManager().deployments.size());
		assertEquals(3, slavesADeployedTo.size());
		assertEquals(2, slavesBDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentA));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentA));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentB));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentB));
	}

	private Slave buildSlave(String name = "Slave", double load = 0, Date lastCheckIn = new Date())
	{
		Slave slave = new Slave();
		slave.slaveName = name;
		slave.loadOnSlave = load;
		slave.lastCheckIn = lastCheckIn;
		return slave;
	}

	private Deployment buildDeployment(String name = "app", String version = "1", int replicationFactor = 1)
	{
		Deployment deployment = new Deployment();
		deployment.applicationName = name;
		deployment.applicationVersion = version;
		deployment.deploymentCommands = "./deploy.sh " + name + "-" + version;
		deployment.replicationFactor = replicationFactor;
		return deployment;
	}
}
