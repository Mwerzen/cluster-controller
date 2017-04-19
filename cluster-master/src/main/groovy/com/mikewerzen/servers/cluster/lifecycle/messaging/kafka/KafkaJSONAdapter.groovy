package com.mikewerzen.servers.cluster.lifecycle.messaging.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.mikewerzen.servers.cluster.lifecycle.messaging.CommandMessage
import com.mikewerzen.servers.cluster.lifecycle.messaging.StatusMessage

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;

class KafkaJSONAdapter implements Serializer<CommandMessage>, Deserializer<StatusMessage> {

	def jsonSlurper = new JsonSlurper();
	def jsonOutput = new JsonOutput();

	void close() {
	}

	void configure(Map<String, ?> configs, boolean isKey) {
	}



	@Override
	public byte[] serialize(String topic, CommandMessage obj) {
		try {
			return jsonOutput.toJson(obj).bytes;
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public StatusMessage deserialize(String topic, byte[] data) {
		try {
			return jsonSlurper.parseText(new String(data, StandardCharsets.UTF_8));
		}
		catch (Exception e) {
			return null;
		}
	}
}
