package com.mikewerzen.servers.cluster.lifecycle

import com.mikewerzen.servers.cluster.lifecycle.messaging.StatusMessage

interface MessagePoller {

	List<StatusMessage> getStatusMessages();
}
