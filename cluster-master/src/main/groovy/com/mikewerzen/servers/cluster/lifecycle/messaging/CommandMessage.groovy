package com.mikewerzen.servers.cluster.lifecycle.messaging;

public class CommandMessage {
	String source;
	String target;
	Command commandType;
	String timestamp;
}
