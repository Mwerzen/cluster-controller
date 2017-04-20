package com.mikewerzen.servers.cluster.lifecycle;

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveManager
import com.mikewerzen.servers.cluster.lifecycle.stubs.SlaveMessengerStub

class SlaveManagerTest {

	SlaveManager manager;
	SlaveMessengerStub stub = new SlaveMessengerStub();
//
//	@Before
//	public void setup() {
//		manager = new SlaveManager();
//		manager.setMessenger(stub);
//		stub.reset();
//	}
//
//	@Test
//	public void addNewSlave() {
//		Slave slave = ClusterTestHelper.getSlave("slave1");
//		manager.handleSlaveUpdate(slave);
//
//		assertEquals(1, manager.slavesInCluster.size());
//	}
//
//	@Test
//	public void getSlaveForDeployment_TwoSlaves() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 5);
//		manager.handleSlaveUpdate(slave);
//		Slave slave2 = ClusterTestHelper.getSlave("slave2", 4);
//		manager.handleSlaveUpdate(slave2);
//
//		assertEquals(slave2, manager.getSlaveForDeployment());
//	}
//
//	@Test
//	public void deployToSlave_DeployNewApplication() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 5);
//		manager.handleSlaveUpdate(slave);
//		Slave slave2 = ClusterTestHelper.getSlave("slave2", 4);
//		manager.handleSlaveUpdate(slave2);
//
//		Application newApp = ClusterTestHelper.getApplication("bro", "2.0");
//		manager.deployToSlave(newApp);
//
//		assertEquals(1, stub.deployedApps.size());
//		assertEquals(1, manager.findSlavesRunningSameVersionOfApplication(newApp).size())
//	}
//
//	@Test
//	public void deployToSlave_updateExistingApplication() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 5, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//		Slave slave2 = ClusterTestHelper.getSlave("slave2", 4, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave2);
//
//		Application newApp = ClusterTestHelper.getApplication("app1", "2.0");
//		manager.deployToSlave(newApp);
//
//		assertEquals(1, stub.deployedApps.size());
//		assertEquals(2, stub.undeployedApps.size());
//		assertEquals(1, manager.findSlavesRunningSameVersionOfApplication(newApp).size())
//	}
//
//	@Test
//	public void handleSlaveUpdate_RedeployShutdownApps() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		stub.reset();
//
//		Slave updatedSlave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 1);
//		println("New Slave: " + updatedSlave)
//		manager.handleSlaveUpdate(updatedSlave);
//
//		assertEquals(1, manager.slavesInCluster.size())
//		assertEquals(1, stub.deployedApps.size());
//		assertEquals(2, manager.getAllDeployedApplications().size())
//		assertEquals("app1", stub.deployedApps.iterator().next().getKey().name);
//	}
//
//	@Test
//	public void rebootSlave_triggersReboot() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Slave slave2 = ClusterTestHelper.getSlave("slave2", 0, SlaveStatus.ACTIVE, 0);
//		manager.handleSlaveUpdate(slave2);
//
//		manager.rebootSlave(slave);
//
//		assertEquals(1, stub.restartedSlaves.size())
//	}
//
//	@Test
//	public void rebootSlave_redeploysApps() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Slave slave2 = ClusterTestHelper.getSlave("slave2", 0, SlaveStatus.ACTIVE, 0);
//		manager.handleSlaveUpdate(slave2);
//
//		manager.rebootSlave(slave);
//
//		assertEquals(2, manager.getAllDeployedApplications().size())
//		assertEquals(2, slave2.appsDeployed.size())
//		assertEquals(2, stub.getDeployedApps().size())
//	}
//
//	@Test
//	public void undeployAllVersionsFromSlave_NoneRunning() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Application app = ClusterTestHelper.getApplication("app3");
//		manager.undeployAllVersionsFromSlave(app, slave);
//
//		assertEquals(2, manager.getAllDeployedApplications().size())
//	}
//
//	@Test
//	public void undeployAllVersionsFromSlave_SameVersionRunning() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Application app = ClusterTestHelper.getApplication("app0");
//		manager.undeployAllVersionsFromSlave(app, slave);
//
//		assertEquals(1, manager.getAllDeployedApplications().size())
//	}
//
//	@Test
//	public void undeployAllVersionsFromSlave_DiffVersionRunning() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Application app = ClusterTestHelper.getApplication("app0", "2");
//		manager.undeployAllVersionsFromSlave(app, slave);
//
//		assertEquals(1, manager.getAllDeployedApplications().size())
//	}
//
//	@Test
//	public void undeployThisVersionFromSlave_NoneRunning() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Application app = ClusterTestHelper.getApplication("app3");
//		manager.undeployThisVersionFromSlave(app, slave);
//
//		assertEquals(2, manager.getAllDeployedApplications().size())
//	}
//
//	@Test
//	public void undeployThisVersionFromSlave_SameVersionRunning() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Application app = ClusterTestHelper.getApplication("app0");
//		manager.undeployThisVersionFromSlave(app, slave);
//
//		assertEquals(1, manager.getAllDeployedApplications().size())
//	}
//
//	@Test
//	public void undeployThisVersionFromSlave_DiffVersionRunning() {
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		manager.handleSlaveUpdate(slave);
//
//		Application app = ClusterTestHelper.getApplication("app0", "2");
//		manager.undeployThisVersionFromSlave(app, slave);
//
//		assertEquals(2, manager.getAllDeployedApplications().size())
//	}
//
//	@Test
//	public void cleanOldSlaves() {
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.SECOND, -65);
//		Date moreThanOneMinuteAgo = cal.getTime();
//
//		Slave slave = ClusterTestHelper.getSlave("slave1", 0, SlaveStatus.ACTIVE, 2);
//		slave.lastUpdate = moreThanOneMinuteAgo
//		manager.handleSlaveUpdate(slave);
//
//		manager.cleanOldSlaves();
//		assertEquals(0, manager.slavesInCluster.size())
//	}
}
