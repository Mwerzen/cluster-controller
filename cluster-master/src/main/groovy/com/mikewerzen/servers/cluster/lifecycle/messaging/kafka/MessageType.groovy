package com.mikewerzen.servers.cluster.lifecycle.messaging.kafka

enum MessageType
{
	STATUS, DEPLOY, UNDEPLOY, FINISHED, FAILED, REBOOT, SHUTDOWN;
}
