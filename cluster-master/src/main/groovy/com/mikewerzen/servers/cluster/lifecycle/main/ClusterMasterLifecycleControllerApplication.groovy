package com.mikewerzen.servers.cluster.lifecycle.main

import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan("com.mikewerzen.servers.cluster")
class ClusterMasterLifecycleControllerApplication {

	static void main(String[] args) {
		SpringApplication.run ClusterMasterLifecycleControllerApplication, args
	}
}
