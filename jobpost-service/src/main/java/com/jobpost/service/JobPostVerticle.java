package com.jobpost.service;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.jobpost.service.model.JobPost;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
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

		SQLClient sqlClient = getSQLCLient();

		// creating validation handler to check mandatory fields are present or not.
		HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create();
		vertx.fileSystem().readFile("src/main/resources/schema.json", result -> {
			if (result.succeeded()) {
				Buffer buff = result.result();
				validationHandler.addJsonBodySchema(buff.toString());
			} else {
				logger.error(result.cause());
			}
		});

		Router router = Router.router(vertx);

		router.route("/").handler(routingContext -> {
			HttpServerResponse resp = routingContext.response();
			resp.putHeader("content-type", "text/html").end("My first Vert.x application");
		});

		// This route adds a job to Kafka queue
		router.route("/jobs").handler(BodyHandler.create());
		router.post("/jobs").produces("application/json").handler(validationHandler).handler(this::addJob)
				.failureHandler(routingContext -> {
					Throwable failure = routingContext.failure();
					if (failure instanceof ValidationException) {
						String message = JobPostConstants.VALIDATION_MESSAGE;
						routingContext.response().setStatusCode(400).end(message);
					}
				});

		// This route deletes a job post
		router.delete("/jobs/:id").handler(routingContext -> {
			deleteJob(routingContext, sqlClient);
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

	private SQLClient getSQLCLient() {
		// Creating MySql connection
		JsonObject dbConfig = new JsonObject()
				.put(JobPostConstants.MYSQL_HOST, System.getenv(JobPostConstants.SYS_HOST))
				.put(JobPostConstants.MYSQL_USERNAME, System.getenv(JobPostConstants.SYS_USERNAME))
				.put(JobPostConstants.MYSQL_PASSWORD, System.getenv(JobPostConstants.SYS_PASSWORD))
				.put(JobPostConstants.MYSQL_DATABASE, System.getenv(JobPostConstants.SYS_DATABASE))
				.put(JobPostConstants.MYSQL_CHARSET, System.getenv(JobPostConstants.SYS_CHARSET))
				.put(JobPostConstants.MYSQL_TIMEOUT, Integer.parseInt(System.getenv(JobPostConstants.SYS_TIMEOUT)));

		logger.info(dbConfig);
		SQLClient sqlClient = MySQLClient.createNonShared(vertx, dbConfig);
		return sqlClient;
	}

	private KafkaProducer<String, JsonObject> getKafkaConsumer() {
		// Connecting to cloudkarafka.
		String username = "3ysi7bdb";
		String password = "Y_f7N4MY6rwtewnjC7mDzZ7HIK42F-df";
		String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
		String jaasCfg = String.format(jaasTemplate, username, password);

		Properties config = new Properties();
		String brokers = "velomobile-01.srvs.cloudkafka.com:9094, velomobile-02.srvs.cloudkafka.com:9094, velomobile-03.srvs.cloudkafka.com:9094";
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
		config.put(ProducerConfig.ACKS_CONFIG, "1");
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "Job_Group");
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put("sasl.jaas.config", jaasCfg);
		config.put("security.protocol", "SASL_SSL");
		config.put("sasl.mechanism", "SCRAM-SHA-256");

		KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, config);
		return producer;
	}

	/*
	 * This function adds a new job into Kafka queue.
	 */
	private void addJob(RoutingContext routingContext) {
		KafkaProducer<String, JsonObject> producer = getKafkaConsumer();
		JobPost jobPost = Json.decodeValue(routingContext.getBodyAsString(), JobPost.class);

		if (jobPost.getSkills() == null) {
			jobPost.setSkills("None");
		}
		if (jobPost.getLanguages() == null) {
			jobPost.setLanguages("English");
		}
		produceRecords(producer, routingContext, jobPost);
	}

	/*
	 * This function deletes a job of given id from the database.
	 */
	private void deleteJob(RoutingContext routingContext, SQLClient sqlClient) {
		int removeId = Integer.valueOf(routingContext.request().getParam("id"));
		JsonArray params = new JsonArray();
		params.add(removeId);

		String deleteQuery = JobPostConstants.DELETE_QUERY;
		sqlClient.getConnection(conn -> {
			if (conn.succeeded()) {
				SQLConnection connection = conn.result();
				connection.queryWithParams(deleteQuery, params, result -> {
					if (result.succeeded()) {
						routingContext.response().end("Deleted the job post");
					} else {
						logger.info("Error in deleting the job post");
					}
					connection.close();
				});
			} else {
				logger.info(conn.cause().getMessage());
			}
		});

	}

	
	private void produceRecords(KafkaProducer<String, JsonObject> producer, RoutingContext routingContext,
			JobPost jobPost) {
		JsonObject jsonObj = new JsonObject(Json.encode(jobPost));

		KafkaProducerRecord<String, JsonObject> rec = KafkaProducerRecord.create("3ysi7bdb-jobs", null, jsonObj);

		producer.write(rec, resp -> {
			if (resp.succeeded()) {
				logger.info("Added job to Kafka queue");
			} else {
				logger.error("Couldn't add job to a queue");
			}
			routingContext.response().end(Json.encodePrettily(jobPost));
		});
	}
}
