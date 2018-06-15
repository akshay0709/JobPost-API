package org.jobpost.service.model;

import java.nio.file.AtomicMoveNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;

public class JobPost {
	
	private long id;
    private String jobName;
    private JobType jobType;
    private String postDate;
    private String country;
    private String language;
    private int payRate;
    //private static final AtomicInteger COUNTER =  new AtomicInteger();
    
    
	public void setId(long id) {
		this.id = id;
	}


	public long getId() {
		return id;
	}
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public JobType getJobType() {
		return jobType;
	}
	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}
	public String getPostDate() {
		return postDate;
	}
	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}
	public int getPayRate() {
		return payRate;
	}
	public void setPayRate(int payRate) {
		this.payRate = payRate;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
    
}
