package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.InternalServerErrorException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class CreditServiceImpl implements CreditService {

    final String CREDIT_SERVICE_UNAVAILABLE_MESSAGE = "Credit service unavailable. Retry again later";

    @Autowired
    @Qualifier("creditWebClient")
    private WebClient creditWebClient;

    @CircuitBreaker(name = "creditService", fallbackMethod = "getCreditByIdFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Mono<CreditResponse> getCreditById(String creditId) {
        return creditWebClient.get()
            .uri("/credit/{creditId}", creditId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ResponseBase.class)
                    .flatMap(error -> {
                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new NotFoundException(error.getMessage()));
                        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                            return Mono.error(new BadRequestException(error.getMessage()));
                        } else {
                            return Mono.error(new InternalServerErrorException(error.getMessage()));
                        }
                    })
            )
            .bodyToMono(CreditResponse.class);
    }

    @CircuitBreaker(name = "creditService", fallbackMethod = "updateCreditByIdFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Mono<CreditResponse> updateCreditById(String creditId, CreditPatchRequest request) {
        return creditWebClient.patch()
            .uri("/credit/{creditId}", creditId)
            .body(Mono.just(request), CreditPatchRequest.class)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ResponseBase.class)
                    .flatMap(error -> {
                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new NotFoundException(error.getMessage()));
                        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                            return Mono.error(new BadRequestException(error.getMessage()));
                        } else {
                            return Mono.error(new InternalServerErrorException(error.getMessage()));
                        }
                    })
            )
            .bodyToMono(CreditResponse.class);
    }

    @CircuitBreaker(name = "creditService", fallbackMethod = "getCreditsByCustomerIdFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Flux<CreditResponse> getCreditsByCustomerId(String customerId) {
        return creditWebClient.get()
            .uri("/credit/by-customer/{customerId}", customerId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ResponseBase.class)
                    .flatMap(error -> {
                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new NotFoundException(error.getMessage()));
                        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                            return Mono.error(new BadRequestException(error.getMessage()));
                        } else {
                            return Mono.error(new InternalServerErrorException(error.getMessage()));
                        }
                    })
            )
            .bodyToFlux(CreditResponse.class);
    }

    // getCreditByIdFallback
    private Mono<CustomerResponse> getCreditByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditByIdFallback(String id, WebClientRequestException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // updateCreditByIdFallback
    private Mono<CustomerResponse> updateCreditByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> updateCreditByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> updateCreditByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> updateCreditByIdFallback(String id, WebClientRequestException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getCreditsByCustomerIdFallback
    private Flux<CustomerResponse> getCreditsByCustomerIdFallback(String id, InternalServerErrorException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<CustomerResponse> getCreditsByCustomerIdFallback(String id, TimeoutException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<CustomerResponse> getCreditsByCustomerIdFallback(String id, CallNotPermittedException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<CustomerResponse> getCreditsByCustomerIdFallback(String id, WebClientRequestException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }
}
