package com.jobpost.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.pattern.PropertiesPatternConverter;

import com.jobpost.service.model.JobPost;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import kafka.cluster.Partition;

public class JobPostVerticle extends AbstractVerticle {

	private final Logger logger = LoggerFactory.getLogger(JobPostVerticle.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new JobPostVerticle());
	}

	@Override
	public void start(Future<Void> future) {
		
		String username = "3ysi7bdb";
		String password = "Y_f7N4MY6rwtewnjC7mDzZ7HIK42F-df";
		String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);
		
		Properties config = new Properties();
		String brokers = "velomobile-01.srvs.cloudkafka.com:9094, velomobile-02.srvs.cloudkafka.com:9094, velomobile-03.srvs.cloudkafka.com:9094";
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
		config.put(ProducerConfig.ACKS_CONFIG, "1");
		/*config.put(ConsumerConfig.GROUP_ID_CONFIG, "Job_Group");
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put("sasl.jaas.config", jaasCfg);
		config.put("security.protocol", "SASL_SSL");
		config.put("sasl.mechanism", "SCRAM-SHA-256");*/
		
		

		KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, config);

		producer.partitionsFor("jobs-topic", resp -> {
			resp.result().forEach(part -> logger.info("This is partition info: id={}, topic={}", part.getPartition(),
					part.getTopic()));
		});

		Router router = Router.router(vertx);
		router.route("/").handler(routingContext -> {
			HttpServerResponse resp = routingContext.response();
			resp.putHeader("content-type", "text/html").end("My first application");
		});

		router.route("/jobs").handler(BodyHandler.create());
		router.post("/jobs").produces("application/json").handler(routingContext -> {
			JobPost jobPost = Json.decodeValue(routingContext.getBodyAsString(), JobPost.class);
			KafkaProducerRecord<String, JsonObject> rec = KafkaProducerRecord.create("jobs-topic", null,
					routingContext.getBodyAsJson());

			producer.write(rec, resp -> {

				if (resp.succeeded()) {
					RecordMetadata recMetadata = resp.result();
					logger.info("Producer created records");
					jobPost.setId(recMetadata.getOffset());
				} else {
					Throwable t = resp.cause();
					logger.error("Could not send topic");
				}
				routingContext.response().end(Json.encodePrettily(jobPost));
			});

		});

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080),
				result -> {
					if (result.succeeded()) {
						future.complete();
					} else {
						future.fail(result.cause());
					}
				});
	}
}
