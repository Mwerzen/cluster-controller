package com.mikewerzen.servers.cluster.lifecycle

import com.mikewerzen.servers.cluster.lifecycle.domain.StatusMessage

interface MessagePoller {

	List<StatusMessage> getStatusMessages();
}
