package com.mikewerzen.servers.cluster.lifecycle;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import com.mikewerzen.servers.cluster.lifecycle.domain.Application
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveManager
import com.mikewerzen.servers.cluster.lifecycle.messaging.DateUtil
import com.mikewerzen.servers.cluster.lifecycle.messaging.StatusMessage;

@Component
public class PollingController  {

	@Autowired
	MessagePoller poller;

	@Autowired
	SlaveManager manager;

	public PollingController() {
	}


	@Scheduled(fixedDelay=10000L)
	public void poll() {
		List<StatusMessage> statusMessages = poller.getStatusMessages();

		for(statusMsg in statusMessages) {

			handleSlaveStatusMessage(statusMsg)
		}

		manager.cleanOldSlaves();
		println("Iteration finished at: " + new Date().toString() + " :: " + manager);
	}

	private void handleSlaveStatusMessage(StatusMessage statusMsg) {
		Slave slave = new Slave();

		try {
			slave.name = statusMsg.source;
			slave.load = statusMsg.load;
			slave.appsDeployed = new HashSet<Application>();
			for(app in statusMsg.applications) {
				Application application = new Application();
				application.name = app.name;
				application.version = app.version;
				application.command = app.command;
				slave.appsDeployed.add(application);
			}

			slave.lastUpdate = DateUtil.convertStringToDate(statusMsg.timestamp);
			manager.handleSlaveUpdate(slave);
		}
		catch (Exception e) {
			println("Exception processing a slave: " + slave + " " + e)
			e.printStackTrace()
		}
	}
}
