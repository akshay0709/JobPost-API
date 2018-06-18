# JobPost-API

 JobPost API is a basic example of RESTful web service designed using Vert.x, Kafka, and MySQL. The application have three working modules - jobpost-service, jobpost-search, and jobpost-consumer. 
 
 jobpost-service - Implements the REST api and Kafka producer to add jobs in the Kafka queue. 
 jobpost-consumer - Implements the Kafka consumer and stores jobs in MySQL db. 
 jobpost-search - Implements REST api to search jobs.
 
 
## Installation steps:
 Install Java, Maven on your machine. Build the code using maven. Run the modules using following commands. 
 
 ### jobpost-search
 ```
 java -jar <path where jar is downloaded>/jobpost-search-0.0.1-SNAPSHOT-fat.jar -conf src/main/config /configuration.json
 ```
 ### jobpost-consumer
 ```
 java -jar <path where the jar is downloaded>/jobpost-consumer-0.0.1-SNAPSHOT-fat.jar -conf src/main /config /configuration.json
 ```
 ### jobpost-service
 ```
 java -jar <path where jar is downloaded>/jobpost-service-0.0.1-SNAPSHOT-fat.jar
 ```
 
