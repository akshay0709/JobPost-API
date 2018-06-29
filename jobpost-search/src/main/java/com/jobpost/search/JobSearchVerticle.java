package com.jobpost.search;

import java.util.Enumeration;
import java.util.Hashtable;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class JobSearchVerticle extends AbstractVerticle {

	private final Logger logger = LoggerFactory.getLogger(JobSearchVerticle.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new JobSearchVerticle());
	}

	@Override
	public void start(Future<Void> future) {

		Router router = Router.router(vertx);
		SQLClient sqlClient = getSQLCLient();

		router.get("/jobs").produces("application/json").handler(routingContext -> {
			String jobName = routingContext.request().getParam(JobSearchConstants.JOB_NAME);
			String location = routingContext.request().getParam(JobSearchConstants.JOB_LOCATION);
			String jobType = routingContext.request().getParam(JobSearchConstants.JOB_AVAIL);
			String postDate = routingContext.request().getParam(JobSearchConstants.JOB_POST_DATE);
			String payRate = routingContext.request().getParam(JobSearchConstants.JOB_PAY_RATE);

			// Generating based on the request parameters. As per the document -
			// https://vertx.io/docs/vertx-mysql-postgresql-client/java/
			// Stored procedures are not supported by MySQLClient.

			StringBuilder query = new StringBuilder();
			JsonArray params = new JsonArray();
			query.append(JobSearchConstants.SELECT_QUERY);

			Hashtable<String, String> paramsTable = new Hashtable<>();
			if (jobName != null)
				paramsTable.put(JobSearchConstants.JOB_NAME, jobName);
			if (location != null)
				paramsTable.put(JobSearchConstants.JOB_LOCATION, location);
			if (jobType != null)
				paramsTable.put(JobSearchConstants.JOB_AVAIL, jobType);
			if (postDate != null)
				paramsTable.put(JobSearchConstants.JOB_POST_DATE, postDate);
			if (payRate != null)
				paramsTable.put(JobSearchConstants.JOB_PAY_RATE, payRate);
			
			if (payRate != null)
				paramsTable.put(JobSearchConstants.JOB_PAY_RATE, payRate);

			if (paramsTable.size() > 0) {
				query.append(" WHERE");
			}

			Enumeration k = paramsTable.keys();

			int count = 0;

			while (k.hasMoreElements()) {
				String sKey = (String) k.nextElement();
				if (count >= 1)
					query.append(" AND");

				String value = paramsTable.get(sKey);
				if (sKey.equals(JobSearchConstants.JOB_NAME)) {
					query.append(" job_title LIKE ?");
					value = "%"+value+"%";
				}
				else if (sKey.equals(JobSearchConstants.JOB_LOCATION))
					query.append(" location=?");
				else if (sKey.equals(JobSearchConstants.JOB_POST_DATE))
					query.append(" post_date=?");
				else if (sKey.equals(JobSearchConstants.JOB_PAY_RATE))
					query.append(" pay_rate=?");
				else if (sKey.equals(JobSearchConstants.JOB_AVAIL))
					query.append(" availability=?");

				params.add(value);
				count += 1;

			}
			logger.info(query);
			logger.info(params);
			getJobs(sqlClient, routingContext, query, params);

		});
		
		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8081),
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
		JsonObject dbConfig = new JsonObject().put(JobSearchConstants.MYSQL_HOST, System.getenv(JobSearchConstants.SYS_HOST))
				.put(JobSearchConstants.MYSQL_USERNAME, System.getenv(JobSearchConstants.SYS_USERNAME))
				.put(JobSearchConstants.MYSQL_PASSWORD, System.getenv(JobSearchConstants.SYS_PASSWORD))
				.put(JobSearchConstants.MYSQL_DATABASE, System.getenv(JobSearchConstants.SYS_DATABASE))
				.put(JobSearchConstants.MYSQL_CHARSET, System.getenv(JobSearchConstants.SYS_CHARSET))
				.put(JobSearchConstants.MYSQL_TIMEOUT, Integer.parseInt(System.getenv(JobSearchConstants.SYS_TIMEOUT)));
	
		logger.info(dbConfig);
		SQLClient sqlClient = MySQLClient.createNonShared(vertx, dbConfig);
		return sqlClient;
	}

	private void getJobs(SQLClient sqlClient, RoutingContext routingContext, StringBuilder query, JsonArray params) {
		sqlClient.getConnection(conn -> {
			logger.info("Database connection established");
			if (conn.succeeded()) {

				SQLConnection connection = conn.result();
				logger.info(query.toString());
				connection.queryWithParams(query.toString(), params, resultHandler -> {

					if (resultHandler.succeeded()) {
						ResultSet result = resultHandler.result();
						if (result == null) {
							routingContext.response().end(JobSearchConstants.NO_JOBS);
						} else {
							routingContext.response().end(resultHandler.result().getResults().toString());
						}

					} else {
						logger.error(resultHandler.cause().getMessage());
					}
					connection.close();
				});
			} else {
				logger.info(conn.cause().getMessage());
			}
		});
	}
}
