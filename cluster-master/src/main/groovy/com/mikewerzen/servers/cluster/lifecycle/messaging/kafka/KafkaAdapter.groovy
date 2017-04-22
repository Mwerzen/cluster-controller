package com.mikewerzen.servers.cluster.lifecycle.messaging.kafka

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component

import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventPoller
import com.mikewerzen.servers.cluster.lifecycle.domain.event.EventRegistry

@Component
class KafkaAdapter implements EventPoller
{


	public static final String MASTER_NAME = "master";

	public static final String KAFKA_IP = "192.168.1.50:9092"

	public static final String KAFKA_TOPIC = "test";

	EventRegistry registry = EventRegistry.getInstance();

	KafkaConsumer consumer;
	KafkaProducer producer;

	private KafkaAdapter()
	{
		Properties consumerProps = new Properties();
		consumerProps.put("bootstrap.servers", KAFKA_IP);
		consumerProps.put("group.id", MASTER_NAME);
		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put("value.deserializer", "com.mikewerzen.servers.cluster.lifecycle.messaging.kafka.KafkaJSONAdapter");


		consumer = new KafkaConsumer<String, Expando>(consumerProps);
		consumer.subscribe(Arrays.asList(KAFKA_TOPIC));


		Properties producerProps = new Properties();
		producerProps.put("bootstrap.servers", KAFKA_IP);
		producerProps.put("retries", 10);
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "com.mikewerzen.servers.cluster.lifecycle.messaging.kafka.KafkaJSONAdapter");

		producer = new KafkaProducer<String, Expando>(producerProps)
	}


	@Override
	public void pollInboundEvents()
	{
		ConsumerRecords<String, Expando> records = consumer.poll(100);
		for(record in records)
		{
			try
			{
				Expando msg = record.value;
				println("Message Read: $msg")
				if (msg.target == MASTER_NAME)
				{

					MessageType type = msg.type.toUpperCase();
					switch (type)
					{
						case MessageType.STATUS:
							registry.addStatusEvent(msg.source, msg.load);
							break;
						case MessageType.FINISHED:
							registry.addFinishedEvent(msg.source, msg.applicationName, msg.applicationVersion);
							break;
						case MessageType.FAILED:
							registry.addFailedEvent(msg.source, msg.applicationName, msg.applicationVersion);
							break;
					}
				}
			} catch (Exception e)
			{
				println("Exception Occured Processing Messages: $e")
			}
		}
		consumer.commitAsync();
		return;
	}

	@Override
	public void pollOutboundEvents()
	{
		for(event in registry.getAndClearDeploymentEvents())
		{
			Expando msg = new Expando();
			msg.source = MASTER_NAME;
			msg.target = event.slave.slaveName;
			msg.type = MessageType.DEPLOY;
			msg.applicationName = event.deployment.applicationName;
			msg.applicationVersion = event.deployment.applicationVersion;
			msg.applicationCommand = event.deployment.deploymentCommands;

			ProducerRecord record = new ProducerRecord<String, Expando>(KAFKA_TOPIC, msg);
			producer.send(record);
		}

		for(event in registry.getAndClearUndeploymentEvents())
		{
			Expando msg = new Expando();
			msg.source = MASTER_NAME;
			msg.target = event.slave.slaveName;
			msg.type = MessageType.UNDEPLOY;
			msg.applicationName = event.deployment.applicationName;
			msg.applicationVersion = event.deployment.applicationVersion;

			ProducerRecord record = new ProducerRecord<String, Expando>(KAFKA_TOPIC, msg);
			producer.send(record);
		}

		for(event in registry.getAndClearRebootEvents())
		{
			Expando msg = new Expando();
			msg.source = MASTER_NAME;
			msg.target = event.slave.slaveName;
			msg.type = MessageType.REBOOT;

			ProducerRecord record = new ProducerRecord<String, Expando>(KAFKA_TOPIC, msg);
			producer.send(record);
		}

		for(event in registry.getAndClearShutdownEvents())
		{
			Expando msg = new Expando();
			msg.source = MASTER_NAME;
			msg.target = event.slave.slaveName;
			msg.type = MessageType.SHUTDOWN;

			ProducerRecord record = new ProducerRecord<String, Expando>(KAFKA_TOPIC, msg);
			producer.send(record);
		}
	}
}
