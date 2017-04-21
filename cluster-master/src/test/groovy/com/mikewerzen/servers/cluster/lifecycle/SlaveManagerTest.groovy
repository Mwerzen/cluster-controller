package com.mikewerzen.servers.cluster.lifecycle

import static com.mikewerzen.servers.cluster.lifecycle.ClusterTestHelper.*;
import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.DeploymentManager
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveManager
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry

class SlaveManagerTest
{
	EventRegistry registry = EventRegistry.getInstance();
	SlaveManager manager;

	@Before
	public void setup()
	{
		resetRegistry();
	}

	@Test
	public void testFindSlaveForName_Valid()
	{
		manager = buildSlaveManager(2);

		assertNotNull(manager.findSlaveForName("Slave0"));
	}

	@Test
	public void testFindSlaveForName_None()
	{
		manager = buildSlaveManager(2);

		assertNull(manager.findSlaveForName("Slave9"));
	}

	@Test
	public void testFindSlaveForName_NameNull()
	{
		manager = buildSlaveManager(2);

		assertNull(manager.findSlaveForName(null));
	}

	@Test
	public void test_registerSlave()
	{
		manager = buildSlaveManager(1);
		manager.registerSlave(buildSlave("newSlave"));

		assertEquals(2, manager.slavesInCluster.size());
	}

	@Test
	public void test_registerSlave_null()
	{
		manager = buildSlaveManager(1);
		manager.registerSlave(null);

		assertEquals(1, manager.slavesInCluster.size());
	}

	@Test
	public void test_isClusterRunningAnyVersionOfDeployment_true()
	{
		ClusterController controller = buildClusterController(3, 1);

		manager = controller.slaveManager;

		Deployment dep = buildDeployment("App0", "Over9000.0");

		assertTrue(manager.isClusterCurrentlyRunningAnyVersionOfDeployment(dep));
	}

	@Test
	public void test_isClusterRunningAnyVersionOfDeployment_false()
	{
		ClusterController controller = buildClusterController(3, 1);

		manager = controller.slaveManager;

		Deployment dep = buildDeployment("App1", "Over9000.0");

		assertFalse(manager.isClusterCurrentlyRunningAnyVersionOfDeployment(dep));
	}

	@Test
	public void test_isClusterRunningSameVersionOfDeployment_true()
	{
		ClusterController controller = buildClusterController(3, 1);

		manager = controller.slaveManager;

		Deployment dep = buildDeployment("App0", "1");

		assertTrue(manager.isClusterCurrentlyRunningSameVersionOfDeployment(dep));
	}

	@Test
	public void test_isClusterRunningSameVersionOfDeployment_false()
	{
		ClusterController controller = buildClusterController(3, 1);

		manager = controller.slaveManager;

		Deployment dep = buildDeployment("App0", "2");

		assertFalse(manager.isClusterCurrentlyRunningSameVersionOfDeployment(dep));
	}

	@Test
	public void test_getNumberOfSlaveRunningSameVersionOfDeployment()
	{
		ClusterController controller = buildClusterController(3, 3, 2, 1);

		manager = controller.slaveManager;

		assertEquals(3, manager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.deploymentManager.findDeployment("App0")));
		assertEquals(2, manager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.deploymentManager.findDeployment("App1")));
		assertEquals(1, manager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.deploymentManager.findDeployment("App2")));
		assertEquals(0, manager.getNumberOfSlavesRunningSameVersionOfDeployment(controller.deploymentManager.findDeployment("App3")));
	}


	@Test
	public void test_findOptimalSlavesForDeployment_SingleSlave()
	{
		manager = buildSlaveManager(5);

		double load = 0;
		manager.slavesInCluster.toSorted().each { slave -> slave.loadOnSlave = load++;};

		def dep = buildDeployment();

		def results = manager.findOptimalSlavesForDeployment(dep);

		assertEquals(1, results.size());
		assertEquals("Slave0", results.get(0).slaveName);
	}

	@Test
	public void test_findOptimalSlavesForDeployment_MultiDeployment()
	{
		manager = buildSlaveManager(5);

		double load = 0;
		manager.slavesInCluster.toSorted().each { slave -> slave.loadOnSlave = load++;};

		def dep = buildDeployment("App", "1", 5);

		def results = manager.findOptimalSlavesForDeployment(dep);
		println(results)
		assertEquals(5, results.size());
		assertEquals("Slave0", results.get(0).slaveName);
		assertEquals("Slave1", results.get(1).slaveName);
		assertEquals("Slave2", results.get(2).slaveName);
		assertEquals("Slave3", results.get(3).slaveName);
		assertEquals("Slave4", results.get(4).slaveName);
		assertTrue(results.get(0).loadOnSlave < results.get(1).loadOnSlave);
		assertTrue(results.get(1).loadOnSlave < results.get(2).loadOnSlave);
		assertTrue(results.get(2).loadOnSlave < results.get(3).loadOnSlave);
		assertTrue(results.get(3).loadOnSlave < results.get(4).loadOnSlave);
	}

	@Test
	public void test_reassignDeploymentToNewSlave()
	{
		manager = buildSlaveManager(5);

		double load = 0;
		manager.slavesInCluster.toSorted().each { slave -> slave.loadOnSlave = load++;};

		def dep = buildDeployment();

		def results = manager.deployToCluster(dep);

		assertEquals(1, results.size());
		assertEquals("Slave0", results.get(0).slaveName);
		assertTrue(results.get(0).isRunningSameVersionOfDeployment(dep));
		assertTrue(manager.findSlaveForName("Slave0").isRunningSameVersionOfDeployment(dep));

		results = manager.reassignDeploymentToNewSlave(dep, manager.findSlaveForName("Slave0"));

		assertEquals(1, results.size());
		assertEquals("Slave1", results.get(0).slaveName);
		assertTrue(results.get(0).isRunningSameVersionOfDeployment(dep));
		assertFalse(manager.findSlaveForName("Slave0").isRunningSameVersionOfDeployment(dep));
	}

	@Test
	public void test_deployToCluster_SingleSlave()
	{
		manager = buildSlaveManager(5);

		double load = 0;
		manager.slavesInCluster.toSorted().each { slave -> slave.loadOnSlave = load++;};

		def dep = buildDeployment();

		def results = manager.deployToCluster(dep);

		assertEquals(1, results.size());
		assertEquals("Slave0", results.get(0).slaveName);
		assertTrue(results.get(0).isRunningSameVersionOfDeployment(dep));
	}

	@Test
	public void test_deployToCluster_MultiDeployment()
	{
		manager = buildSlaveManager(8);

		double load = 0;
		manager.slavesInCluster.toSorted().each { slave -> slave.loadOnSlave = load++;};

		def dep = buildDeployment("App", "1", 5);

		def results = manager.deployToCluster(dep);
		println(results)
		assertEquals(5, results.size());
		assertEquals("Slave0", results.get(0).slaveName);
		assertEquals("Slave1", results.get(1).slaveName);
		assertEquals("Slave2", results.get(2).slaveName);
		assertEquals("Slave3", results.get(3).slaveName);
		assertEquals("Slave4", results.get(4).slaveName);
		assertTrue(results.get(0).isRunningSameVersionOfDeployment(dep));
		assertTrue(results.get(1).isRunningSameVersionOfDeployment(dep));
		assertTrue(results.get(2).isRunningSameVersionOfDeployment(dep));
		assertTrue(results.get(3).isRunningSameVersionOfDeployment(dep));
		assertTrue(results.get(4).isRunningSameVersionOfDeployment(dep));
		assertTrue(results.get(0).loadOnSlave < results.get(1).loadOnSlave);
		assertTrue(results.get(1).loadOnSlave < results.get(2).loadOnSlave);
		assertTrue(results.get(2).loadOnSlave < results.get(3).loadOnSlave);
		assertTrue(results.get(3).loadOnSlave < results.get(4).loadOnSlave);
		assertEquals(5, manager.getNumberOfSlavesRunningSameVersionOfDeployment(dep));
	}

	@Test
	public void test_undeployThisVersionFromCluster_DiffVersion()
	{
		ClusterController controller = buildClusterController(3, 3, 3);

		manager = controller.slaveManager;

		def dep = buildDeployment("App0", "Over9000.0");
		manager.undeployFromCluster(dep, false);

		assertEquals(0, registry.getAndClearUndeploymentEvents().size());
		assertEquals(2, manager.findSlaveForName("Slave0").deploymentsRunning.size());
	}

	@Test
	public void test_undeployThisVersionFromCluster_SameVersion()
	{
		ClusterController controller = buildClusterController(3, 3, 3);

		manager = controller.slaveManager;

		def dep = buildDeployment("App0", "1");
		manager.undeployFromCluster(dep, false);

		assertEquals(3, registry.getAndClearUndeploymentEvents().size());
		assertEquals(1, manager.findSlaveForName("Slave0").deploymentsRunning.size());
	}

	@Test
	public void test_undeployThisVersionFromCluster_NotInCluster()
	{
		ClusterController controller = buildClusterController(3, 3, 3);

		manager = controller.slaveManager;

		def dep = buildDeployment("App4", "Over9000.0");
		manager.undeployFromCluster(dep, false);

		assertEquals(0, registry.getAndClearUndeploymentEvents().size());
		assertEquals(2, manager.findSlaveForName("Slave0").deploymentsRunning.size());
	}


	@Test
	public void test_undeployAllVersionsFromCluster_DiffVersion()
	{
		ClusterController controller = buildClusterController(3, 3, 3);

		manager = controller.slaveManager;

		def dep = buildDeployment("App0", "Over9000.0");
		manager.undeployFromCluster(dep, true);

		assertEquals(3, registry.getAndClearUndeploymentEvents().size());
		assertEquals(1, manager.findSlaveForName("Slave0").deploymentsRunning.size());
	}

	@Test
	public void test_undeployAllVersionsFromCluster_SameVersion()
	{
		ClusterController controller = buildClusterController(3, 3, 3);

		manager = controller.slaveManager;

		def dep = buildDeployment("App0", "1");
		manager.undeployFromCluster(dep, true);

		assertEquals(3, registry.getAndClearUndeploymentEvents().size());
		assertEquals(1, manager.findSlaveForName("Slave0").deploymentsRunning.size());
	}

	@Test
	public void test_undeployAllVersionsFromCluster_NotInCluster()
	{
		ClusterController controller = buildClusterController(3, 3, 3);

		manager = controller.slaveManager;

		def dep = buildDeployment("App4", "Over9000.0");
		manager.undeployFromCluster(dep, true);

		assertEquals(0, registry.getAndClearUndeploymentEvents().size());
		assertEquals(2, manager.findSlaveForName("Slave0").deploymentsRunning.size());
	}

	@Test
	public void test_rebalanceSlaves_NoneNeeded()
	{
		ClusterController controller = buildClusterController(3, 3, 2);

		manager = controller.slaveManager;
		manager.rebalanceSlaves(controller.deploymentManager.deployments);

		assertEquals(0, registry.getAndClearUndeploymentEvents().size());
		assertEquals(0, registry.getAndClearDeploymentEvents().size());
	}

	@Test
	public void test_rebalanceSlaves_TwoNeeded()
	{
		ClusterController controller = buildClusterController(3, 1);
		controller.deploymentManager.deployments.each { it.replicationFactor = 3};

		manager = controller.slaveManager;
		manager.rebalanceSlaves(controller.deploymentManager.deployments);

		assertEquals(0, registry.getAndClearUndeploymentEvents().size());
		assertEquals(2, registry.getAndClearDeploymentEvents().size());
	}


	@Test
	public void test_killDeadSlaves()
	{
		ClusterController controller = buildClusterController(3, 3, 2);

		manager = controller.slaveManager;
		manager.slavesInCluster.iterator().next().lastCheckInMillis = System.currentTimeMillis() - (2 * 60 * 60 * 1000L);

		println(manager)
		manager.shutdownDeadSlaves();
		println(manager)

		assertEquals(2, manager.slavesInCluster.size());
		assertEquals(2, registry.getAndClearUndeploymentEvents().size());
	}
	
	@Test
	public void test_rebootslave()
	{
		ClusterController controller = buildClusterController(3, 3);
		
		manager = controller.slaveManager;
		manager.rebootSlave(controller.findSlave("Slave0"));
		
		assertEquals(1, registry.getUndeploymentEvents().size());
		assertEquals(1, registry.getRebootEvents().size());
		assertEquals(2, manager.slavesInCluster.size());
	}
	
	@Test
	public void test_rebootslave_byName()
	{
		ClusterController controller = buildClusterController(3, 3);
		
		manager = controller.slaveManager;
		manager.rebootSlave("Slave0");
		
		assertEquals(1, registry.getUndeploymentEvents().size());
		assertEquals(1, registry.getRebootEvents().size());
		assertEquals(2, manager.slavesInCluster.size());
	}
}
