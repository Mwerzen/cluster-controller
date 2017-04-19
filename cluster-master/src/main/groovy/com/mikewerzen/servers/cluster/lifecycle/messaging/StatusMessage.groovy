package com.mikewerzen.servers.cluster.lifecycle.messaging;

import com.mikewerzen.servers.cluster.lifecycle.domain.Application

public class StatusMessage {
	String source;
	double load;
	List<Application> applications;
	String timestamp;
}
