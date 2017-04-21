package com.mikewerzen.servers.cluster.lifecycle

import static com.mikewerzen.servers.cluster.lifecycle.ClusterTestHelper.*;
import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.mikewerzen.servers.cluster.lifecycle.domain.DeploymentManager
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry

class DeploymentManagerTest
{
	EventRegistry registry = EventRegistry.getInstance();
	DeploymentManager manager;

	@Before
	public void setup()
	{
		resetRegistry();
	}

	@Test
	public void test_addDeployment_newDeployment()
	{
		manager = buildDeploymentManager(2, 2, 2);

		manager.addDeployment(buildDeployment("App5"), true);

		assertEquals(4, manager.deployments.size());
	}

	@Test
	public void test_addDeployment_newVersionDeployment()
	{
		manager = buildDeploymentManager(2, 2, 2);

		manager.addDeployment(buildDeployment("App1", "3.0"), true);

		assertEquals(4, manager.deployments.size());
	}

	@Test
	public void test_addDeployment_newVersionOverwrites()
	{
		manager = buildDeploymentManager(2, 2, 2);

		manager.addDeployment(buildDeployment("App1", "3.0"), false);

		assertEquals(3, manager.deployments.size());
		assertEquals("3.0", manager.findDeployment("App1").applicationVersion);
	}

	@Test
	public void test_addDeployment_existingVersionOverwrites()
	{
		manager = buildDeploymentManager(2, 2, 2);

		manager.addDeployment(buildDeployment("App1", "1", 4), false);

		assertEquals(3, manager.deployments.size());
		assertEquals(4, manager.findDeployment("App1").replicationFactor);
	}

	@Test
	public void test_addDeployment_existingVersionAddsOn()
	{
		manager = buildDeploymentManager(2, 2, 2);

		manager.addDeployment(buildDeployment("App1", "1", 4), true);

		assertEquals(3, manager.deployments.size());
		assertEquals(6, manager.findDeployment("App1").replicationFactor);
	}

	@Test
	public void test_findDeployment_Name()
	{
		manager = buildDeploymentManager(2, 2, 2);
		assertNotNull(manager.findDeployment("App1"));
	}

	@Test
	public void test_findDeployment_NameNull()
	{
		manager = buildDeploymentManager(2, 2, 2);
		assertNull(manager.findDeployment("App3"));
	}

	@Test
	public void test_findDeployment_NameVersion()
	{
		manager = buildDeploymentManager(2, 2, 2);
		assertNotNull(manager.findDeployment("App1", "1"));
	}

	@Test
	public void test_findDeployment_NameVersionNull()
	{
		manager = buildDeploymentManager(2, 2, 2);
		assertNull(manager.findDeployment("App3", "2"));
	}

	@Test
	public void test_isApplicationDeployed_SameVersion_Exists()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App1", "1");
		assertTrue(manager.isApplicationDeployed(dep, false));
	}

	@Test
	public void test_isApplicationDeployed_SameVersion_Null()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App1", "2");
		assertFalse(manager.isApplicationDeployed(dep, false));
	}

	@Test
	public void test_isApplicationDeployed_AnyVersion_Exists()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App1", "2");
		assertTrue(manager.isApplicationDeployed(dep, true));
	}

	@Test
	public void test_isApplicationDeployed_AnyVersion_Null()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App3", "2");
		assertFalse(manager.isApplicationDeployed(dep, true));
	}

	@Test
	public void test_removeDeployment_removeAnyVersion()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App1", "2");
		
		manager.removeDeployment(dep, true);
		
		assertEquals(2, manager.deployments.size());
	}
	
	@Test
	public void test_removeDeployment_removeAnyVersion_DoesntExist()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App4", "2");
		
		manager.removeDeployment(dep, true);
		
		assertEquals(3, manager.deployments.size());
	}
	
	@Test
	public void test_removeDeployment_removeSameVersion()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App1", "1");
		
		manager.removeDeployment(dep, false);
		
		assertEquals(2, manager.deployments.size());
	}
	
	@Test
	public void test_removeDeployment_removeSameVersion_DoesntExist()
	{
		manager = buildDeploymentManager(2, 2, 2);
		def dep = buildDeployment("App1", "2");
		
		manager.removeDeployment(dep, false);
		
		assertEquals(3, manager.deployments.size());
	}
}
