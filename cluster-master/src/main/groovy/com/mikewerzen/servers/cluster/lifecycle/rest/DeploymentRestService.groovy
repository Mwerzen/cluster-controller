package com.mikewerzen.servers.cluster.lifecycle.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.domain.exception.ClusterIntegrityException

@RestController
@RequestMapping("/")
class DeploymentRestService
{

	@Autowired
	ClusterController controller;
	
	Gson gson = new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().create();

	@RequestMapping(path="/deploy", method = RequestMethod.POST)
	public ResponseEntity deployApplication(@RequestBody DeploymentCommand body)
	{
		try
		{
			Deployment deployment = new Deployment();
			deployment.applicationName = body.applicationName;
			deployment.applicationVersion = body.applicationVersion;
			deployment.deploymentCommands = body.applicationCommand;
			deployment.replicationFactor = body.replicationFactor;
			controller.deploy(deployment, body.keepOldVersions);
			return new ResponseEntity(HttpStatus.OK);
		}
		catch (ClusterIntegrityException e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
		catch (Exception x)
		{
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		return true;
	}

	@RequestMapping(path="/undeploy", method = RequestMethod.DELETE)
	public ResponseEntity undeployApplication(@RequestParam(value="name") String name, @RequestParam(value="version", required=false) String version)
	{
		try
		{
			controller.undeploy(controller.findDeployment(name, version), version == null);
			return new ResponseEntity(HttpStatus.OK);
		}
		catch (Exception e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	@RequestMapping(path="/rebootCluster", method = RequestMethod.DELETE)
	public ResponseEntity rebootCluster()
	{
		try
		{
			controller.rebootCluster();
			return new ResponseEntity(HttpStatus.OK);
		}
		catch (Exception e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	@RequestMapping(path="/terminateCluster", method = RequestMethod.DELETE)
	public ResponseEntity terminateCluster()
	{
		try
		{
			controller.terminateCluster();
			return new ResponseEntity(HttpStatus.OK);
		}
		catch (Exception e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	@RequestMapping(path="/deploymentsToSlaves", method = RequestMethod.GET)
	public ResponseEntity getDeploymentsToSlaves()
	{
		try
		{
			Map depToSlaves = controller.getDeploymentsToSlaves();
			String body = gson.toJson(depToSlaves);
			return new ResponseEntity(body, HttpStatus.OK);
		}
		catch (Exception e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	@RequestMapping(path="/slavesToDeployments", method = RequestMethod.GET)
	public ResponseEntity getSlavesToDeployments()
	{
		try
		{
			Map slavesToDep = controller.getSlavesToDeployments();
			String body = gson.toJson(slavesToDep);
			return new ResponseEntity(body, HttpStatus.OK);
		}
		catch (Exception e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@RequestMapping(path="/clusterInfo", method = RequestMethod.GET)
	public ResponseEntity getClusterInfo()
	{
		try
		{
			String body = gson.toJson(controller);
			return new ResponseEntity(body, HttpStatus.OK);
		}
		catch (Exception e)
		{
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
}
