package com.mikewerzen.servers.cluster.lifecycle.messaging;

import static org.junit.Assert.*

import com.mikewerzen.servers.cluster.lifecycle.messaging.kafka.KafkaJSONAdapter
import groovy.json.JsonOutput
import org.junit.Ignore
import org.junit.Test

@Ignore
class TestKafkaJSONAdapter
{

	KafkaJSONAdapter jsonAdapter = new KafkaJSONAdapter();

	@Test
	public void testDeserialization()
	{
		String json = "{\"source\": \"118\", \"load\": 1, \"timestamp\": \"2017-04-17 22:28:28\", \"applications\": [{\"name\": \"testAPp\", \"version\": \"1\", \"command\": \"DeployMe\"}]}";
		def msg = jsonAdapter.deserialize("test", json.bytes);

		println(msg.source);
	}

	@Test
	public void testSerialization()
	{
		def msg = new Expando();
		msg.source = "118";
		
		println(new JsonOutput().toJson(msg));
		println(jsonAdapter.serialize("test", msg));
	}
}
