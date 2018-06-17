package com.jobpost.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jobpost.service.JobPostVerticle;
import com.jobpost.service.model.JobPost;
import com.jobpost.service.model.JobType;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class JobPostVerticleTest {
	private Vertx vertx;

	private static final Logger logger = LoggerFactory.getLogger(JobPostVerticleTest.class);

	@Before
	public void setUp(TestContext context) {
		vertx = Vertx.vertx();
		vertx.deployVerticle(JobPostVerticle.class.getName(), context.asyncAssertSuccess());
	}

	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testApplication(TestContext context) {
		final Async async = context.async();

		vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
			response.handler(body -> {
				context.assertTrue(body.toString().contains("My first"));
				async.complete();
			});
		});
	}

	@Test
	public void testPostJobs(TestContext context) {
		Async async = context.async();

		JobPost temp = new JobPost();
		temp.setId(1);
		temp.setJobName("Java Developer");
		temp.setJobType(JobType.FullTime);
		temp.setPostDate("16/05/2018");
		temp.setCountry("US");
		temp.setLanguage("English");
		temp.setPayRate(60);

		String jsonString = Json.encodePrettily(temp);
		vertx.createHttpClient().post(8080, "localhost", "/jobs").putHeader("Content-Type", "application/json")
				.putHeader("Content-Length", Integer.toString(jsonString.length())).handler(response -> {

					context.assertEquals(response.statusCode(), 200);

					response.bodyHandler(body -> {
						logger.info("Response");
						JobPost jobPost = Json.decodeValue(body.toString(), JobPost.class);
						logger.info(jobPost);
						context.assertEquals(temp.getJobName(), jobPost.getJobName());
						async.complete();
					});
				}).write(jsonString).end();
	}
}
