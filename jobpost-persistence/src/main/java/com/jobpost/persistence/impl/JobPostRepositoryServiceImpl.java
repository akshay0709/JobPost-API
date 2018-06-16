package com.jobpost.persistence.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

public class JobPostRepositoryServiceImpl {
	protected final SQLClient client;
	
	public JobPostRepositoryServiceImpl(Vertx vertx, JsonObject config) {
		this.client = MySQLClient.createNonShared(vertx, config);
	}
	
	protected Future<SQLConnection> getConnection() {
	    Future<SQLConnection> future = Future.future();
	    client.getConnection(future.completer());
	    return future;
	}
	
	protected void insertRecord() {
		
	}
	
	protected void getAllRecords() {
		
	}
}
