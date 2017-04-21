package com.mikewerzen.servers.cluster.lifecycle.messaging.kafka

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import com.mikewerzen.servers.cluster.lifecycle.MessagePoller
import com.mikewerzen.servers.cluster.lifecycle.domain.Slave
import com.mikewerzen.servers.cluster.lifecycle.messaging.CommandMessage
import com.mikewerzen.servers.cluster.lifecycle.messaging.DateUtil
import com.mikewerzen.servers.cluster.lifecycle.messaging.StatusMessage

//@Component
class KafkaAdapter //implements MessagePoller, SlaveMessenger {
{

	public static final String MASTER_NAME = "master";

	public static final String KAFKA_IP = "192.168.1.50:9092"

	public static final String KAFKA_COMMAND_TOPIC = "test";
	public static final String KAFKA_STATUS_TOPIC = "test";

	KafkaConsumer consumer;
	KafkaProducer producer;

	private KafkaAdapter() {
		Properties consumerProps = new Properties();
		consumerProps.put("bootstrap.servers", KAFKA_IP);
		consumerProps.put("group.id", MASTER_NAME);
		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put("value.deserializer", "com.mikewerzen.servers.cluster.lifecycle.messaging.kafka.KafkaJSONAdapter");


		consumer = new KafkaConsumer<String, StatusMessage>(consumerProps);
		consumer.subscribe(Arrays.asList(KAFKA_STATUS_TOPIC));


		Properties producerProps = new Properties();
		producerProps.put("bootstrap.servers", KAFKA_IP);
		producerProps.put("retries", 10);
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "com.mikewerzen.servers.cluster.lifecycle.messaging.kafka.KafkaJSONAdapter");

		producer = new KafkaProducer<String, CommandMessage>(producerProps)
	}
	
	def getMessages()
	{
		
	}

	//	@Override
	//	public void sendDeployCommand(Application application, Slave slave) {
	//		CommandMessage message = new CommandMessage();
	//		message.source = MASTER_NAME;
	//		message.target = slave.name;
	//		message.commandType = Command.DEPLOY;
	//		message.application = application;
	//		message.timestamp = DateUtil.getCurrentDate();
	//
	//		ProducerRecord record = new ProducerRecord<String, CommandMessage>(KAFKA_COMMAND_TOPIC, message);
	//		producer.send(record);
	//	}
	//
	//	@Override
	//	public void sendRebootCommand(Slave slave) {
	//		CommandMessage message = new CommandMessage();
	//		message.source = MASTER_NAME;
	//		message.target = slave.name;
	//		message.commandType = Command.REBOOT;
	//		message.timestamp = DateUtil.getCurrentDate();
	//
	//		ProducerRecord record = new ProducerRecord<String, CommandMessage>(KAFKA_COMMAND_TOPIC, message);
	//		producer.send(record);
	//	}
	//
	//	@Override
	//	public void sendUndeployCommand(Application application, Slave slave) {
	//		CommandMessage message = new CommandMessage();
	//		message.source = MASTER_NAME;
	//		message.target = slave.name;
	//		message.commandType = Command.UNDEPLOY;
	//		message.application = application;
	//		message.timestamp = DateUtil.getCurrentDate();
	//
	//		ProducerRecord record = new ProducerRecord<String, CommandMessage>(KAFKA_COMMAND_TOPIC, message);
	//		producer.send(record);
	//	}
	//
	//	@Override
	//	public List<StatusMessage> getStatusMessages() {
	//		ConsumerRecords<String, StatusMessage> records = consumer.poll(100);
	//		List<StatusMessage> statusMessages = new ArrayList<StatusMessage>();
	//		for(record in records) {
	//			if(record.value)
	//				statusMessages.add(record.value());
	//		}
	//		consumer.commitAsync();
	//		return statusMessages;
	//	}
}
