package com.mikewerzen.servers.cluster.lifecycle.domain;

import static com.mikewerzen.servers.cluster.lifecycle.domain.ClusterTestHelper.*;
import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry
import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException;


public class ClusterControllerDeploymentTests
{
	ClusterController controller;
	EventRegistry registry = EventRegistry.getInstance();

	@Before
	public void setup()
	{
		resetRegistry();
	}

	@Test
	public void test_AddDeployment_NoServersInCluster()
	{
		controller = buildClusterController(0, null);

		Deployment deployment = buildDeployment();

		def slavesDeployedTo;
		try
		{
			slavesDeployedTo = controller.deploy(deployment);
			fail();
		}
		catch (ClusterIntegrityException e)
		{
			assertEquals(0, registry.getAndClearDeploymentEvents().size());
			assertEquals(0, controller.getDeploymentManager().deployments.size());
			assertNull(slavesDeployedTo);
			assertFalse(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
		}
	}

	@Test
	public void test_AddDeployment_SingleServerInCluster()
	{
		controller = buildClusterController(1, null);

		Deployment deployment = buildDeployment();
		def slavesDeployedTo = controller.deploy(deployment);

		assertEquals(1, registry.getAndClearDeploymentEvents().size());
		assertEquals(1, controller.getDeploymentManager().deployments.size());
		assertEquals(1, slavesDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deployment));
	}

	@Test
	public void test_AddDeployment_TwoServersInCluster()
	{
		controller = buildClusterController(2, null);
		assertEquals(2, controller.getSlaveManager().slavesInCluster.size());

		Deployment deployment = buildDeployment();
		def slavesDeployedTo = controller.deploy(deployment);

		assertEquals(1, registry.getAndClearDeploymentEvents().size());
		assertEquals(1, controller.getDeploymentManager().deployments.size());
		assertEquals(1, slavesDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deployment));
	}

	@Test
	public void test_AddDeploymentReplicationFactor2_TwoServersInCluster()
	{
		controller = buildClusterController(2, null);
		assertEquals(2, controller.getSlaveManager().slavesInCluster.size());

		Deployment deployment = buildDeployment("App", "1", 2);
		def slavesDeployedTo = controller.deploy(deployment);

		assertEquals(2, registry.getAndClearDeploymentEvents().size());
		assertEquals(1, controller.getDeploymentManager().deployments.size());
		assertEquals(2, slavesDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deployment));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deployment));
	}

	@Test
	public void test_AddDeploymentWithReplicationFactor2_ThreeServersInCluster()
	{
		controller = buildClusterController(3, null);

		Deployment deployment = buildDeployment("App", "1", 2);
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
		controller = buildClusterController(3, null);
		assertEquals(0, controller.getDeploymentManager().deployments.size());

		Deployment deploymentA = buildDeployment("AppA", "1", 4);
		def slavesADeployedTo = controller.deploy(deploymentA);
		assertEquals(3, registry.getAndClearDeploymentEvents().size());
		assertEquals(1, controller.getDeploymentManager().deployments.size());

		Deployment deploymentB = buildDeployment("AppB", "2", 2);
		def slavesBDeployedTo = controller.deploy(deploymentB);

		assertEquals(2, registry.getAndClearDeploymentEvents().size());
		assertEquals(2, controller.getDeploymentManager().deployments.size());
		assertEquals(3, slavesADeployedTo.size());
		assertEquals(2, slavesBDeployedTo.size());
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentA));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentA));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentB));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentB));
	}

	@Test
	public void test_AddDeploymentWithReplicationFactor5Then4Then8_7ServersInCluster()
	{
		controller = buildClusterController(7, 5);
		assertEquals(1, controller.getDeploymentManager().deployments.size());

		Deployment deploymentA = buildDeployment("AppA", "1", 5);
		def slavesADeployedTo = controller.deploy(deploymentA);
		assertEquals(5, slavesADeployedTo.size());
		assertEquals(5, registry.getAndClearDeploymentEvents().size());
		assertEquals(2, controller.getDeploymentManager().deployments.size());

		Deployment deploymentB = buildDeployment("AppB", "2", 4);
		def slavesBDeployedTo = controller.deploy(deploymentB);
		assertEquals(4, slavesBDeployedTo.size());
		assertEquals(4, registry.getAndClearDeploymentEvents().size());
		assertEquals(3, controller.getDeploymentManager().deployments.size());

		Deployment deploymentC = buildDeployment("AppC", "2", 9);
		def slavesCDeployedTo = controller.deploy(deploymentC);
		assertEquals(7, slavesCDeployedTo.size());
		assertEquals(7, registry.getAndClearDeploymentEvents().size());
		assertEquals(4, controller.getDeploymentManager().deployments.size());

		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentA));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentA));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentB));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentB));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningAnyVersionOfDeployment(deploymentC));
		assertTrue(controller.getSlaveManager().isClusterCurrentlyRunningSameVersionOfDeployment(deploymentC));
	}
}
