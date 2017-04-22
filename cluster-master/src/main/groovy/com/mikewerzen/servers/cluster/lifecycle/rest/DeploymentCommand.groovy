package com.mikewerzen.servers.cluster.lifecycle.rest;

public class DeploymentCommand
{
	public String applicationName;
	public String applicationVersion;
	public String applicationCommand;
	public int replicationFactor;
	public boolean keepOldVersions;

}
