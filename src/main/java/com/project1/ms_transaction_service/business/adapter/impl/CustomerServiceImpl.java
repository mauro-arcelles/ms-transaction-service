package com.project1.ms_transaction_service.business.adapter.impl;

import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.InternalServerErrorException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.CustomerResponse;
import com.project1.ms_transaction_service.model.ResponseBase;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class CustomerServiceImpl implements CustomerService {

    final String CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE = "Customer service unavailable. Retry again later";

    @Autowired
    @Qualifier("customerWebClient")
    private WebClient webClient;

    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByDniFallback")
    @TimeLimiter(name = "customerService")
    @Override
    public Mono<CustomerResponse> getCustomerByDni(String dni) {
        return webClient.get()
            .uri("/dni/{dni}", dni)
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
            .bodyToMono(CustomerResponse.class);
    }

    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByRucFallback")
    @TimeLimiter(name = "customerService")
    @Override
    public Mono<CustomerResponse> getCustomerByRuc(String ruc) {
        return webClient.get()
            .uri("/ruc/{ruc}", ruc)
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
            .bodyToMono(CustomerResponse.class);
    }

    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByIdFallback")
    @TimeLimiter(name = "customerService")
    @Override
    public Mono<CustomerResponse> getCustomerById(String id) {
        return webClient.get()
            .uri("/{id}", id)
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
            .bodyToMono(CustomerResponse.class);
    }

    // getCustomerByDniFallback
    private Mono<CustomerResponse> getCustomerByDniFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByDniFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByDniFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByDniFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getCustomerByRucFallback
    private Mono<CustomerResponse> getCustomerByRucFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByRucFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByRucFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByRucFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getCustomerByIdFallback
    private Mono<CustomerResponse> getCustomerByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(CUSTOMER_SERVICE_UNAVAILABLE_MESSAGE));
    }
}
