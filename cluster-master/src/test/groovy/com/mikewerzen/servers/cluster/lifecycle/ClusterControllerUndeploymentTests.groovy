package com.mikewerzen.servers.cluster.lifecycle;

import static com.mikewerzen.servers.cluster.lifecycle.ClusterTestHelper.*;
import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry
import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException;

public class ClusterControllerUndeploymentTests
{
	ClusterController controller;
	EventRegistry registry = EventRegistry.getInstance();

	@Before
	public void setup()
	{
		resetRegistry();
	}

	@Test
	public void test_RemoveDeployment_OneServersOneReplicationFactor()
	{
		controller = buildClusterController(1, 1);

		def deployment = controller.findDeployment("App0", null);

		controller.undeploy(deployment, true);

		assertEquals(0, controller.deploymentManager.deployments.size());
		assertEquals(1, controller.getSlavesToDeployments().size());
		assertEquals(0, controller.getSlavesToDeployments().get(controller.findSlave("Slave0")).size());
		assertEquals(0, controller.getDeploymentsToSlaves().size());
		assertEquals(1, registry.getAndClearUndeploymentEvents().size());
	}

	@Test
	public void test_RemoveDeployment_ThreeServersOneReplicationFactor()
	{
		controller = buildClusterController(3, 1);

		def deployment = controller.findDeployment("App0", null);

		controller.undeploy(deployment, true);

		assertEquals(0, controller.deploymentManager.deployments.size());
		assertEquals(3, controller.getSlavesToDeployments().size());
		assertEquals(0, controller.getSlavesToDeployments().get(controller.findSlave("Slave0")).size());
		assertEquals(0, controller.getDeploymentsToSlaves().size());
		assertEquals(1, registry.getAndClearUndeploymentEvents().size());
	}

	@Test
	public void test_RemoveDeployment_ThreeServersTwoAppsTwoReplicationFactor()
	{
		controller = buildClusterController(3, 2, 2);
		println(controller.getDeploymentsToSlaves());

		def deployment = controller.findDeployment("App0", null);

		controller.undeploy(deployment, true);

		assertEquals(1, controller.deploymentManager.deployments.size());
		assertEquals(3, controller.getSlavesToDeployments().size());
		println(controller.getSlavesToDeployments());
		assertEquals(1, controller.getSlavesToDeployments().get(controller.findSlave("Slave2")).size());
		assertEquals(1, controller.getDeploymentsToSlaves().size());
		assertEquals(2, controller.getDeploymentsToSlaves().get(controller.findDeployment("App1", null)).size());
		assertEquals(2, registry.getAndClearUndeploymentEvents().size());
	}
}
