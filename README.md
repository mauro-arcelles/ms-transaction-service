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
Service connects to Config Server using:
```properties
spring.application.name=ms-transaction-service
spring.config.import=optional:configserver:http://localhost:8888
```
for properties
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: ms-transaction-service

server:
  port: 8092

application:
  config:
    account-service-url: http://localhost:8091/api/v1/accounts
    credit-service-url: http://localhost:8093/api/v1/credits
    customer-service-url: http://localhost:8090/api/v1/customers
```

## Swagger
http://localhost:8092/swagger-ui.html

![ms-transaction-service-2025-02-11-233353](https://github.com/user-attachments/assets/930663b1-24a2-4030-8792-7e5eb366b2c4)

