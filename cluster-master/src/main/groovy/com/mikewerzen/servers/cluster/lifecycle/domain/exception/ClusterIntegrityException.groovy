package com.mikewerzen.servers.cluster.lifecycle.domain.exception

class ClusterIntegrityException extends RuntimeException
{
	String message;

	public ClusterIntegrityException(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return "ClusterIntegrityException [message=" + message + "]";
	}
}
