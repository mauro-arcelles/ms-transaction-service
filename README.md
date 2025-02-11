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

![ms-transaction-service-2025-02-11-233353](https://github.com/user-attachments/assets/930663b1-24a2-4030-8792-7e5eb366b2c4)

