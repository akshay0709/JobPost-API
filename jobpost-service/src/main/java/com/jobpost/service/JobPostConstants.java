package com.jobpost.service;

public class JobPostConstants {
	public static final String SELECT_QUERY="SELECT * FROM JobPost";
	public static final String DELETE_QUERY = "Delete from JobPost where id=?";
	
	
	public static final String MYSQL_HOST="host";
	public static final String MYSQL_USERNAME="username";
	public static final String MYSQL_PASSWORD="password";
	public static final String MYSQL_DATABASE="database";
	public static final String MYSQL_CHARSET="charset";
	public static final String MYSQL_TIMEOUT="queryTimeout";
	
	public static final String SYS_HOST="DB_HOST";
	public static final String SYS_USERNAME="DB_USERNAME";
	public static final String SYS_PASSWORD="DB_PASSWORD";
	public static final String SYS_DATABASE="DB_DATABASE";
	public static final String SYS_CHARSET="DB_CHARSET";
	public static final String SYS_TIMEOUT="DB_TIMEOUT";
	
	public static final String VALIDATION_MESSAGE = "Please enter details - job title, availability, pay rate, post date, location, and company name";
}
