package com.mikewerzen.servers.cluster.lifecycle.rest

import javax.management.modelmbean.RequiredModelMBean

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import com.mikewerzen.servers.cluster.lifecycle.domain.Application
import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveManager

@Controller
@RequestMapping("/")
class RestController {

	@Autowired
	SlaveManager manager;

	@RequestMapping(path="/deploy")
	public ResponseEntity<Boolean> deployApplication(@RequestParam(value="name") String name, @RequestParam(value="version") String version,
			@RequestParam(value="command") String command) {

		Application application = new Application();
		application.name = name;
		application.version = version;
		application.command = command;

		manager.deployToSlave(application);

		return null;
	}

	@RequestMapping(path="/undeploy")
	public boolean undeployApplication(@RequestParam(value="name") String name, @RequestParam(value="version", required=false) String version) {

		Application application = new Application();
		application.name = name;
		application.version = version;

		if (version)
			manager.undeployThisVersionFromAllSlaves(application);
		else
			manager.undeployAllVersionsFromAllSlaves(application);

		return null;
	}
}
