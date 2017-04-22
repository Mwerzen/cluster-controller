package com.mikewerzen.servers.cluster.lifecycle.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

import com.mikewerzen.servers.cluster.lifecycle.domain.ClusterController
import com.mikewerzen.servers.cluster.lifecycle.domain.Deployment
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave

@Controller
@RequestMapping("/")
class RestController
{

	@Autowired
	ClusterController controller;

	@RequestMapping(path="/deploy", method = RequestMethod.POST)
	public ResponseEntity<Boolean> deployApplication(@RequestBody Expando body)
	{
		println("Started")
		Deployment deployment = new Deployment();
		deployment.applicationName = body.applicationName;
		deployment.applicationVersion = body.applicationVersion;
		deployment.deploymentCommands = body.applicationCommand;
		deployment.replicationFactor = body.replicationFactor;

		controller.deploy(deployment, body.keepOldVersions);

		return true;
	}

	@RequestMapping(path="/undeploy", method = RequestMethod.DELETE)
	public boolean undeployApplication(@RequestParam(value="name") String name, @RequestParam(value="version", required=false) String version)
	{
		controller.undeploy(controller.findDeployment(name, version), version == null);

		return true;
	}

	@RequestMapping(path="/rebootCluster", method = RequestMethod.PUT)
	public boolean rebootCluster()
	{
		controller.rebootCluster();

		return true;
	}

	@RequestMapping(path="/terminateCluster", method = RequestMethod.PUT)
	public boolean terminateCluster()
	{
		controller.terminateCluster();

		return true;
	}

	@RequestMapping(path="/deploymentsToSlaves", method = RequestMethod.GET)
	public ResponseEntity<Map<Deployment, Slave>> getDeploymentsToSlaves()
	{
		ResponseEntity<Map<Deployment, Slave>> res = new ResponseEntity<Map<Deployment, Slave>>();
		res.body = controller.getDeploymentsToSlaves();
		return res;
	}
}
