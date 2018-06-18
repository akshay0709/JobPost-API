# JobPost-API

 JobPost API is a basic example of RESTful web service designed using Vert.x, Kafka, and MySQL(AWS RDS). The application has three working modules - jobpost-service, jobpost-search, and jobpost-consumer. 
 
 jobpost-service - Implements the REST API and Kafka producer to add jobs in the Kafka queue. 
 jobpost-consumer - Implements the Kafka consumer and stores jobs in MySQL db. 
 jobpost-search - Implements REST API to search jobs.
 
 
## Installation steps:
 Install Java, Maven on your machine. Export AWS MySQL configuration as system environment variables from configuration .json to your machine. Build the code using maven. Run the modules using following commands. 
 
 ### jobpost-search
 ```
 java -jar <path where jar is downloaded>/jobpost-search-0.0.1-SNAPSHOT-fat.jar
 ```
 ### jobpost-consumer
 ```
 java -jar <path where the jar is downloaded>/jobpost-consumer-0.0.1-SNAPSHOT-fat.jar
 ```
 ### jobpost-service
 ```
 java -jar <path where jar is downloaded>/jobpost-service-0.0.1-SNAPSHOT-fat.jar
 ```
 

 
