package com.jobpost.persistence.model;


public class JobPost {
	
	private long id;
    private String jobName;
    private JobType jobType;
    private String postDate;
    private String country;
    private String language;
    private int payRate;
    
    public JobPost() {
    }
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public int getPayRate() {
		return payRate;
	}
	public void setPayRate(int payRate) {
		this.payRate = payRate;
	}
        
}
