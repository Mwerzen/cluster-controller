package com.mikewerzen.servers.cluster.lifecycle.domain.event

interface EventPoller
{
	void pollInboundEvents();
	void pollOutboundEvents();
}
