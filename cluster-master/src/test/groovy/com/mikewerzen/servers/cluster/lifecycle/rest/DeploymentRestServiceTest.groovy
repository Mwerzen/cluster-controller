package com.mikewerzen.servers.cluster.lifecycle.rest;

import static com.mikewerzen.servers.cluster.lifecycle.domain.ClusterTestHelper.*

import org.junit.Test

import com.google.gson.Gson
import com.google.gson.GsonBuilder

public class DeploymentRestServiceTest
{

	@Test
	public void serializeController()
	{
		Gson gson = new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().create();
		println(gson.toJson(buildClusterController(3, 1, 1, 2).getDeploymentsToSlaves()));
	}
}
