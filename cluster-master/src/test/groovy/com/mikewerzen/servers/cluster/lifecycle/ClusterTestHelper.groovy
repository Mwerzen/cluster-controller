package com.mikewerzen.servers.cluster.lifecycle

import com.mikewerzen.servers.cluster.lifecycle.domain.Application
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveStatus

class ClusterTestHelper {

	static Application getApplication(String name, String version = "1") {
		Application app = new Application();
		app.name = name;
		app.version = version;
		app.command = "Test Command";
		return app;
	}

	static Slave getSlave(String name, double load = 0, SlaveStatus status = SlaveStatus.ACTIVE, int appsDeployed = 0) {
		Slave slave = new Slave();
		slave.name = name;
		slave.load = load;
		for(int i = 0; i < appsDeployed; i++) {
			slave.appsDeployed.add(getApplication("app" + i));
		}
		slave.lastUpdate = new Date();
		return slave;
	}
}
