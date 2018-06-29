package com.jobpost.consumer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.jobpost.consumer.model.JobPost;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;

public class JobsConsumerVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(JobsConsumerVerticle.class);

	public static void main(String args[]) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new JobsConsumerVerticle());
	}

	@Override
	public void start() throws Exception {

		KafkaConsumer<String, String> consumer = getKafkaConsumer();

		SQLClient mySQLClient = getSQLClient();
		consumer.subscribe("3ysi7bdb-jobs", resp -> {
			if (resp.succeeded()) {
				logger.info("Topic subscribed");
			} else {
				logger.error("Error in subscribing");
			}
		});

		consumer.handler(rec -> {
			JobPost jobPost = Json.decodeValue(rec.value(), JobPost.class);

			mySQLClient.getConnection(conn -> {
				logger.info("Database connection established");
				if (conn.succeeded()) {
					SQLConnection connection = conn.result();
					try {
						insert(jobPost, connection);
					} catch (ParseException e) {

						e.printStackTrace();
					}
				} else {
					logger.info(conn.cause().getMessage());
				}
			});
		});

	}

	private KafkaConsumer<String, String> getKafkaConsumer() {
		// Connecting to cloudkarafka(Message streaming as a Service)

		String username = "3ysi7bdb";
		String password = "Y_f7N4MY6rwtewnjC7mDzZ7HIK42F-df";
		String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
		String jaasCfg = String.format(jaasTemplate, username, password);

		Properties config = new Properties();
		String brokers = "velomobile-01.srvs.cloudkafka.com:9094, velomobile-02.srvs.cloudkafka.com:9094, velomobile-03.srvs.cloudkafka.com:9094";
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "Job_Group");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		config.put("sasl.jaas.config", jaasCfg);
		config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		config.put("security.protocol", "SASL_SSL");
		config.put("sasl.mechanism", "SCRAM-SHA-256");

		KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
		return consumer;
	}

	private SQLClient getSQLClient() {
		// Creating MySql connection - AWS
		JsonObject dbConfig = new JsonObject()
				.put(JobConsumerConstants.MYSQL_HOST, System.getenv(JobConsumerConstants.SYS_HOST))
				.put(JobConsumerConstants.MYSQL_USERNAME, System.getenv(JobConsumerConstants.SYS_USERNAME))
				.put(JobConsumerConstants.MYSQL_PASSWORD, System.getenv(JobConsumerConstants.SYS_PASSWORD))
				.put(JobConsumerConstants.MYSQL_DATABASE, System.getenv(JobConsumerConstants.SYS_DATABASE))
				.put(JobConsumerConstants.MYSQL_CHARSET, System.getenv(JobConsumerConstants.SYS_CHARSET))
				.put(JobConsumerConstants.MYSQL_TIMEOUT,
						Integer.parseInt(System.getenv(JobConsumerConstants.SYS_TIMEOUT)));

		SQLClient mySQLClient = MySQLClient.createNonShared(vertx, dbConfig);
		return mySQLClient;
	}

	private void insert(JobPost jobPost, SQLConnection connection) throws ParseException {
		String sql = "INSERT INTO JobPost (job_title, availability, post_date, location, language, pay_rate, skills, company_name, exp_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		long millis = System.currentTimeMillis();
		if (jobPost.getPostDate() != null) {
			Date postDate = formatter.parse(jobPost.getPostDate());
			millis = postDate.getTime();
		}

		logger.info(jobPost.getJobTitle());
		logger.info(jobPost.getLanguages());
		logger.info(jobPost.getAvailability());
		logger.info(jobPost.getCompanyName());
		logger.info(jobPost.getExperienceLevel());

		JsonArray jsonArray = new JsonArray().add(jobPost.getJobTitle()).add(jobPost.getAvailability().getName())
				.add(Instant.ofEpochMilli(millis)).add(jobPost.getLocation()).add(jobPost.getLanguages())
				.add(jobPost.getPayRate()).add(jobPost.getSkills()).add(jobPost.getCompanyName())
				.add(jobPost.getExperienceLevel());

		connection.updateWithParams(sql, jsonArray, resp -> {
			if (resp.failed()) {
				logger.info("Failed to insert");
			}
			logger.info("Inserted a job post into db ");

			connection.close();
		});
	}

}
