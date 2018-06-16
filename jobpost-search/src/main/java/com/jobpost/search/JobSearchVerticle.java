package com.jobpost.search;



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
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;



public class JobSearchVerticle extends AbstractVerticle{
	
	private final Logger logger = LoggerFactory.getLogger(JobSearchVerticle.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new JobSearchVerticle());
	}

	@Override
	public void start(Future<Void> future) {
		
		Router router = Router.router(vertx);
		
		JsonObject sqlConfig = new JsonObject()
				.put("host", "jobs-mysqlinstance.cuo0vhbk0bzr.us-west-1.rds.amazonaws.com")
				.put("username", "user")
				.put("password","Test1234")
				.put("database", "jobsDB")
				.put("charset", "UTF-8")
				.put("queryTimeout", 10000);
		
		SQLClient mySQLClient = MySQLClient.createNonShared(vertx, sqlConfig);
		
		router.get("/jobs").produces("application/json").handler(routingContext -> {
			String jobName = routingContext.request().getParam("jobName");
			String location = routingContext.request().getParam("location");
			JsonArray params = new JsonArray().add(jobName).add(location);
			logger.info(jobName);
			mySQLClient.getConnection(conn ->{
				logger.info("connected");
				if (conn.succeeded()) {
			        SQLConnection connection = conn.result();
			        String getAllProc = "{call GetAllJobs()}";
			        String selectByLocAndJobName = "SELECT * FROM JOBPOST WHERE JOB_NAME=? AND COUNTRY=?";
			        connection.queryWithParams(selectByLocAndJobName, params, resultHandler -> {
			        	if(resultHandler.succeeded()) {
			        		ResultSet rs = resultHandler.result();
			        		logger.info(rs);
			        		routingContext.response().end(rs.toString());
			        	} else {
			        		//logger.error("Couldn't get data");
			        	}
			        });
			    }else {
			    	//logger.info(conn.cause().getMessage());
			    }
			});
			
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
}
