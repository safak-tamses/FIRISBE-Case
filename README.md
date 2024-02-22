# FIRISBE - Java Backend Developer - Test Case:

This project comprises a Spring Boot-based backend application that simulates an online payment system. The project fulfills the requirements outlined in the given case.

A combination of SQL and NoSQL database types is utilized within the project. NoSQL is employed for database logging operations, while SQL data databases manage customer and transfer transactions. Postgresql is selected as the SQL database, and MongoDB serves as the NoSQL database.

To reflect real-life transaction scenarios in the payment system, transfer transactions are conducted both asynchronously and within transaction structures. Apache Kafka application is utilized for asynchronous data operations.

A Docker container is created to accommodate all project dependencies. Access to the container is available through the docker-compose.yml file located in the root directory. To utilize it, execute the "docker compose up -d" command from the directory containing the file.

The project's unit tests are completed using the JUnit library. Additionally, to access the project's endpoints, you can simply double-click the "Swagger UI.html" file located in the root directory, and navigate to the Swagger endpoints with their respective descriptions provided.

## Requirements

- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-downloads.html) (for the Backend)
- [PostgreSQL](https://www.postgresql.org/) (SQL Database)
- [MongoDB](https://www.mongodb.com/) (NoSQL Database)
- [Kafka](https://kafka.apache.org/) (Kafka for asyn process)

## Docker Setup

If you prefer using Docker for the database:

1. Navigate to the directory containing docker-compose.yml named backend.

2. Run the following command in the console to set up the database:

```bash
docker-compose up -d
```
3. Once done, the database will be ready to be used in the project.

---

## Backend (Spring Boot) Setup


1. Navigate to the `FIRISBE-Case` folder: `cd /FIRISBE-Case`.

2. To set up the PostgreSQL database, configure the `application.properties` file located at `FIRISBE-Case/src/main/resources/application.yml` with the necessary database connection details:
3. To set up the MongoDB database, configure the `application.properties` file located at `FIRISBE-Case/src/main/resources/application.yml` with the necessary database connection details:
4. To set up the Kafka database, configure the `application.properties` file located at `FIRISBE-Case/src/main/resourcess/application.yml` with the necessary database connection details:

```bash
  spring:
  kafka:
    bootstrap-servers: localhost:9092

  main:
    allow-bean-definition-overriding: true


  data:
    mongodb:
      host: localhost
      port: 27017
      database: crud
      username: root
      password: example
      authentication-database: admin

  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5433/crud
    username: root
    password: example

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

kafka:
  topic:
    success: successful_logs
    error: error_logs
    paymentLog: payment_log
    paymentProcess: payment_process

  groupId: groupId

```

## To start the application:
1. After configuring the database, build and install the required dependencies for the Spring project using Maven:

```bash
mvn clean install
```

2. Start the Spring Boot application by running the following command:

```bash
java -jar FIRISBE-Case/target/application.jar
```

This command will launch the Spring Boot application.

## API Documentation

-> You can review the required technical requirements in the project from the following file: `Firisbe_case_study.pdf` located at the root path.

->  To explore the backend API endpoints and make requests, you can access the Swagger documentation at `http://localhost:8080/swagger-ui/index.html`.

## Contact

->  For any questions or feedback regarding the project, you can contact us at safak.tamses@gmail.com .



