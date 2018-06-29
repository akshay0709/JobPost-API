package com.jobpost.consumer.model;

public enum JobType {
	
	FullTime("Full-time"),
    PartTime("Part-time"),
    Hourly("Hourly");

    private String name;

    JobType(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
