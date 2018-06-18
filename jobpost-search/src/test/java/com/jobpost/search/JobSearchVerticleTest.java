package com.jobpost.search;

import java.awt.List;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jobpost.search.model.JobPost;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class JobSearchVerticleTest {
	private Vertx vertx;

	private static final Logger logger = LoggerFactory.getLogger(JobSearchVerticleTest.class);

	@Before
	public void setUp(TestContext context) {
		vertx = Vertx.vertx();

		JsonObject conf = new JsonObject().put("host", "jobs-mysqlinstance.cuo0vhbk0bzr.us-west-1.rds.amazonaws.com")
				.put("username", "user").put("password", "Test1234").put("database", "jobsDB").put("charset", "UTF-8")
				.put("queryTimeout", 100000);
		vertx.deployVerticle(JobSearchVerticle.class.getName(), new DeploymentOptions().setConfig(conf),
				context.asyncAssertSuccess());
	}

	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testGetJobs(TestContext context) {
		Async async = context.async();

		vertx.createHttpClient().get(8081, "localhost", "/jobs").putHeader("Content-Type", "application/json")
				.putHeader("keyword", "Java Developer").putHeader("location", "US").handler(response -> {
					context.assertEquals(response.statusCode(), 200);
					response.bodyHandler(body -> {
						JsonArray result = body.toJsonArray();
						logger.info(result);

						async.complete();
					});
				}).end();
	}
}
