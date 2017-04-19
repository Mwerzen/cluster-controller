package com.mikewerzen.servers.cluster.lifecycle.domain;

import com.mikewerzen.servers.cluster.lifecycle.domain.old.Application
import com.mikewerzen.servers.cluster.lifecycle.domain.old.Slave

public interface SlaveMessenger {

	public void sendDeployCommand(Application application, Slave slave);

	public void sendUndeployCommand(Application application, Slave slave);
	
	public void sendRebootCommand(Slave slave);
}
