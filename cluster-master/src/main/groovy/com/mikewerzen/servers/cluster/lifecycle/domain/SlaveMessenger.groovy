package com.mikewerzen.servers.cluster.lifecycle.domain;

public interface SlaveMessenger {

	public void sendDeployCommand(Application application, Slave slave);

	public void sendUndeployCommand(Application application, Slave slave);
	
	public void sendRebootCommand(Slave slave);
}
