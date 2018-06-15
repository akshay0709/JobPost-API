package org.jobpost.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jobpost.consumer.model.JobPost;

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
	public void start() throws Exception{
		String username = "3ysi7bdb";
		String password = "Y_f7N4MY6rwtewnjC7mDzZ7HIK42F-df";
		String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);
		
		Properties config = new Properties();
		String brokers = "velomobile-01.srvs.cloudkafka.com:9094, velomobile-02.srvs.cloudkafka.com:9094, velomobile-03.srvs.cloudkafka.com:9094";
		
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.7:9092");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "Job_Group");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		/*config.put("sasl.jaas.config", jaasCfg);
		config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
		config.put("security.protocol", "SASL_SSL");
		config.put("sasl.mechanism", "SCRAM-SHA-256");*/
		
		JsonObject jsonMySQLconfig = new JsonObject().put("host", "jobs-mysqlinstance.cuo0vhbk0bzr.us-west-1.rds.amazonaws.com")
				.put("port", 3306)
				.put("user", "user")
				.put("password","Test1234");
		
		SQLClient mySQLClient = MySQLClient.createNonShared(vertx, jsonMySQLconfig);
		
		//3ysi7bdb-jobs
		KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
		consumer.subscribe("jobs-topic", resp -> {
			if (resp.succeeded()) {
				
				logger.info("Topic subscribed");
			} else {
				logger.error("Error in subscribing: mess={}", resp.cause().getMessage());
			}
		});
		
		List<JobPost> jobsList = new ArrayList<>();
		
		consumer.handler(rec -> {
			JobPost jobPost = Json.decodeValue(rec.value(), JobPost.class);
			jobsList.add(jobPost);
			logger.info("Consumner Job post : id={}, name={}, type={}", jobPost.getId(), jobPost.getJobName(), jobPost.getJobType());
		});
		
		
		mySQLClient.getConnection(conn ->{
			if (conn.succeeded()) {
		        SQLConnection connection = conn.result();
		        insert(jobsList.get(0),connection);
		    }else {
		    	logger.info("Connection failed : message = {}",conn.cause().getMessage());
		    }
		});
	}
	
	private void insert(JobPost jobPost, SQLConnection connection) {
	    String sql = "INSERT INTO JobPost (job_name, job_type, post_date, country, language, pay_rate) VALUES ?, ?, ?, ?, ?, ?";
	    connection.updateWithParams(sql,
	        new JsonArray().add(jobPost.getId())
	    	.add(jobPost.getJobName())
	    	.add(jobPost.getJobType())
	    	.add(jobPost.getPostDate())
	    	.add(jobPost.getCountry())
	    	.add(jobPost.getLanguage())
	    	.add(jobPost.getPayRate()),
	        (resp) -> {
	          if (resp.failed()) {
	            logger.info("Failed to insert");
	          }
	          UpdateResult result = resp.result();
	          logger.info("Inserted a job post into db ");
	          
	          connection.close();
	        });
	  }


}
