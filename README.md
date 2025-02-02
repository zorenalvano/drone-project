# Drone Delivery System

This is a Spring Boot application that models a drone-based delivery system for urgent package delivery, particularly in areas with difficult access. The system allows you to register drones, load them with medication, check drone battery levels, and more.

## Features

- Register a drone.
- Load a drone with medication.
- Check loaded medications for a specific drone.
- Check drone availability for loading.
- Check the battery level of a drone.

## Pre-loaded Data
- By default, the application comes with 10 pre-loaded drone records in the database. These drones will be available immediately for testing the various API endpoints once the application starts up.

## Build Instructions

1. Build the project using Maven:
    - mvn clean install

2. If you don't have Maven installed globally, you can also run it using the Maven Wrapper:
    - ./mvnw clean install   # For macOS/Linux
    - mvnw.cmd clean install  # For Windows

## Run the Application

1. Once the build is successful, you can run the application using:
    - mvn spring-boot:run

2. Or, if you're using the Maven Wrapper:
    - ./mvnw spring-boot:run   # For macOS/Linux
    - mvnw.cmd spring-boot:run  # For Windows 

3. The application will start on port 8080 by default. You can change the port by modifying the application.yml file
    - server:
        port: 8080

## Run Test

1. To run unit test only, using maven:
    - mvn test
2. Or, running unit test only using the Maven Wrapper:
    - ./mvnw test   # For macOS/Linux
    - mvnw.cmd test  # For Windows 
3. To run unit test and integration test, using maven:
    - mvn verify
4. Or, running unit test and integration test using the Maven Wrapper:
    - ./mvnw verify   # For macOS/Linux
    - mvnw.cmd verify  # For Windows 


