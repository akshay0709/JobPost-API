package com.jobpost.consumer.model;

import java.nio.file.AtomicMoveNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;

public class JobPost {
	
	private long id;
    private String jobTitle;
    private JobType availability;
    private String postDate;
    private String location;
    private String languages;
    private int payRate;
    private int experienceLevel;
    private String companyName;
    private String skills;
    //private static final AtomicInteger COUNTER =  new AtomicInteger();
    
    
	public String getSkills() {
		return skills;
	}


	public void setSkills(String skills) {
		this.skills = skills;
	}


	public void setId(long id) {
		this.id = id;
	}


	public long getId() {
		return id;
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
	
	public String getJobTitle() {
		return jobTitle;
	}


	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}


	public JobType getAvailability() {
		return availability;
	}


	public void setAvailability(JobType availability) {
		this.availability = availability;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public int getExperienceLevel() {
		return experienceLevel;
	}


	public void setExperienceLevel(int experienceLevel) {
		this.experienceLevel = experienceLevel;
	}


	public String getCompanyName() {
		return companyName;
	}


	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}


	public String getLanguages() {
		return languages;
	}
	public void setLanguages(String languages) {
		this.languages = languages;
	}
    
}
