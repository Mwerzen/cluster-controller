package com.mikewerzen.servers.cluster.lifecycle.messaging;

import com.mikewerzen.servers.cluster.lifecycle.domain.Application
import com.mikewerzen.servers.cluster.lifecycle.domain.Command

public class CommandMessage {
	String source;
	String target;
	Command commandType;
	Application application;
	String timestamp;
}
