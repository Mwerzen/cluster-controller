package com.mikewerzen.servers.cluster.lifecycle.stubs

import com.mikewerzen.servers.cluster.lifecycle.domain.Application
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveMessenger

class SlaveMessengerStub implements SlaveMessenger {

	def deployedApps = new HashMap<Application, Slave>();
	def undeployedApps = new HashMap<Slave, Application>();
	def restartedSlaves = new HashSet<Slave>();

	@Override
	public void sendDeployCommand(Application application, Slave slave) {
		deployedApps.put(application, slave);
		println("Deploying: " + application.name + ":" + application.version + " on " + slave.name);
		slave.appsDeployed.add(application);
	}

	@Override
	public void sendUndeployCommand(Application application, Slave slave) {
		undeployedApps.put(slave, application);
		println("Undeploying: " + application.name + ":" + application.version + " on " + slave.name);
		slave.appsDeployed.remove(application);
	}

	@Override
	public void sendRebootCommand(Slave slave) {
		restartedSlaves.add(slave);
		println("Rebooting: " + slave.name);
		slave.appsDeployed.clear();
		slave.load = 0;
	}

	void reset() {
		deployedApps = new HashMap<Application, Slave>();
		undeployedApps = new HashMap<Slave, Application>();
		restartedSlaves = new HashSet<Slave>();
	}
}
