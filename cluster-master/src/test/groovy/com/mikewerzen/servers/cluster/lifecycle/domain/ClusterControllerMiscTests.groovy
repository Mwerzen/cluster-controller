package com.mikewerzen.servers.cluster.lifecycle.domain

import org.junit.Before
import org.junit.Test

import static com.mikewerzen.servers.cluster.lifecycle.domain.ClusterTestHelper.*
import static org.junit.Assert.*
import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry
import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException;

class ClusterControllerMiscTests
{

	ClusterController controller;
	EventRegistry registry = EventRegistry.getInstance();

	@Before
	public void setup()
	{
		resetRegistry();
	}

	@Test
	public void test_findDeployment_NoVersion()
	{
		controller = buildClusterController(2, 3, 3);

		assertNotNull(controller.findDeployment("App0", null));
	}

	@Test
	public void test_findDeployment_SpecificVersion()
	{
		controller = buildClusterController(2, 3, 3);

		assertNotNull(controller.findDeployment("App0", "1"));
	}

	@Test
	public void test_findDeployment_VersionNotFound()
	{
		controller = buildClusterController(2, 3, 3);

		assertNull(controller.findDeployment("App0", "2"));
	}

	@Test
	public void test_handleFailedDeployment_NoSlave()
	{
		controller = buildClusterController(1, 1);

		try
		{
			controller.handleFailedDeployment(controller.findDeployment("App0", null), controller.findSlave("Slave0"));
			fail();
		}
		catch (ClusterIntegrityException e)
		{
		}
	}

	@Test
	public void test_handleFailedDeployment_OneSlaveToTransferTo()
	{
		controller = buildClusterController(2, 1);

		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));

		controller.handleFailedDeployment(controller.findDeployment("App0", null), controller.findSlave("Slave0"));

		assertEquals(1, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
	}

	@Test
	public void test_handleFailedDeployment_TwoSlaveToTransferTo()
	{
		controller = buildClusterController(3, 2);

		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));

		controller.handleFailedDeployment(controller.findDeployment("App0", null), controller.findSlave("Slave2"));

		assertEquals(2, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
	}

	@Test
	public void test_rebalanceCluster_NoAction()
	{
		controller = buildClusterControllerLoadBalanced(3, 2);

		assertEquals(2, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));

		controller.rebalanceCluster();
		println(controller)

		assertEquals(2, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
	}

	@Test
	public void test_rebalanceCluster_AddDeployment()
	{
		controller = buildClusterController(3, 2);

		assertEquals(2, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));

		controller.deploymentManager.findDeployment("App0").replicationFactor = 3;
		controller.rebalanceCluster();

		assertEquals(3, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
	}

	@Test
	public void test_rebalanceCluster_KillDeadSlave()
	{
		controller = buildClusterController(3, 2);

		assertEquals(2, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave0").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertFalse(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));

		controller.findSlave("Slave0").lastCheckInMillis = System.currentTimeMillis() - (60 * 60 * 1000L);
		controller.deploymentManager.findDeployment("App0").replicationFactor = 3;
		
		Thread.sleep(1);
		controller.rebalanceCluster();

		assertEquals(2, controller.slaveManager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertNull(controller.findSlave("Slave0"));
		assertTrue(controller.findSlave("Slave2").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
		assertTrue(controller.findSlave("Slave1").isRunningSameVersionOfDeployment(controller.findDeployment("App0", null)));
	}

	@Test
	public void test_refreshSlave_Exists()
	{
		controller = buildClusterController(3, 2);
		long time1 = controller.findSlave("Slave0").lastCheckInMillis;
		assertEquals(0, controller.findSlave("Slave0").loadOnSlave, 1);

		Thread.sleep(1);
		controller.refreshSlave("Slave0", 2);
		long time2 = controller.findSlave("Slave0").lastCheckInMillis;
		assertEquals(2, controller.findSlave("Slave0").loadOnSlave, 1);

		assertTrue(time1 != time2);
	}
	
	@Test
	public void test_refreshSlave_Null()
	{
		controller = buildClusterController(3, 2);
		controller.refreshSlave("Slave5", 2);

	}
}
