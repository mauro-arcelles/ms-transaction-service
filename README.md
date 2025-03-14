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
- Spring Kafka

## Configuration
Service connects to Config Server using:
```properties
spring.application.name=ms-transaction-service
spring.config.import=optional:configserver:http://localhost:8888
```
for properties
```yaml
eureka:
  instance:
    hostname: localhost
    instance-id: ${spring.application.name}:${random.int}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: ms-bootcamp-arcelles

server:
  port: ${PORT:0}

application:
  config:
    account-service-url: http://ms-account-service/api/v1/accounts
    credit-service-url: http://ms-credit-service/api/v1/credits
    customer-service-url: http://ms-customer-service/api/v1/customers
    kafka:
      bootstrap-servers: localhost:9092
      consumer:
        group-id: wallet-transaction-consumer
      topic-name: wallet-transactions

management:
  endpoints:
    web:
      exposure:
        include: health,circuitbreakerevents
  endpoint:
    health:
      show-details: always

resilience4j:
  circuitbreaker:
    instances:
      customerService:
        slidingWindowSize: 3
        failureRateThreshold: 100
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        ignoreExceptions:
          - com.project1.ms_transaction_service.exception.NotFoundException
          - com.project1.ms_transaction_service.exception.BadRequestException
      creditService:
        slidingWindowSize: 3
        failureRateThreshold: 100
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        ignoreExceptions:
          - com.project1.ms_transaction_service.exception.NotFoundException
          - com.project1.ms_transaction_service.exception.BadRequestException
      accountService:
        slidingWindowSize: 3
        failureRateThreshold: 100
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        ignoreExceptions:
          - com.project1.ms_transaction_service.exception.NotFoundException
          - com.project1.ms_transaction_service.exception.BadRequestException
  timelimiter:
    instances:
      customerService:
        timeoutDuration: 2s
        cancelRunningFuture: true
      creditService:
        timeoutDuration: 2s
        cancelRunningFuture: true
      accountService:
        timeoutDuration: 2s
        cancelRunningFuture: true

springdoc:
  api-docs:
    path: /transaction-docs/v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
  webjars:
    prefix:
```

## Swagger
http://localhost:8092/swagger-ui.html

![ms-transaction-service-2025-03-14-152122](https://github.com/user-attachments/assets/6477754e-dbf1-4860-b80f-72bb98eaa3d1)



