# Transaction Microservice

Spring Boot Webflux microservice that handles transactions operations (deposits, withdrawals, credit card payments, queries).

## Stack
- Java 11
- Spring Boot 2.x
- Spring Webflux
- Spring Cloud Config Client
- Reactive Mongodb
- Openapi contract first
- Swagger ui

## Configuration
Service connects to Config Server for properties:
```properties
spring.application.name=ms-transaction-service
spring.config.import=optional:configserver:http://localhost:8888
```

## Swagger
http://localhost:8092/swagger-ui.html
