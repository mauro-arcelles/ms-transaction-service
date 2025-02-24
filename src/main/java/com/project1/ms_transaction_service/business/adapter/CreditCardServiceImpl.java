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
public class CreditCardServiceImpl implements CreditCardService {

    final String CREDIT_SERVICE_UNAVAILABLE_MESSAGE = "Credit service unavailable. Retry again later";

    @Autowired
    @Qualifier("creditWebClient")
    private WebClient creditWebClient;

    @CircuitBreaker(name = "creditService", fallbackMethod = "getCreditCardByCardNumberFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Mono<CreditCardResponse> getCreditCardByCardNumber(String cardNumber) {
        return creditWebClient.get()
            .uri("/credit-card/by-card-number/{cardNumber}", cardNumber)
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
            .bodyToMono(CreditCardResponse.class);
    }

    @CircuitBreaker(name = "creditService", fallbackMethod = "updateCreditCardFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Mono<CreditCardResponse> updateCreditCard(String id, CreditCardPatchRequest request) {
        return creditWebClient.patch()
            .uri("/credit-card/{id}", id)
            .body(Mono.just(request), CreditCardPatchRequest.class)
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
            .bodyToMono(CreditCardResponse.class);
    }

    @CircuitBreaker(name = "creditService", fallbackMethod = "getCreditCardsByCustomerIdFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Flux<CreditCardResponse> getCreditCardsByCustomerId(String customerId) {
        return creditWebClient.get()
            .uri("/credit-card/by-customer/{customerId}", customerId)
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
            .bodyToFlux(CreditCardResponse.class);
    }

    @CircuitBreaker(name = "creditService", fallbackMethod = "getCreditCardByIdFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Mono<CreditCardResponse> getCreditCardById(String creditCardId) {
        return creditWebClient.get()
            .uri("/credit-card/{creditCardId}", creditCardId)
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
            .bodyToMono(CreditCardResponse.class);
    }

    // getCreditCardByCardNumberFallback
    private Mono<CustomerResponse> getCreditCardByCardNumberFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditCardByCardNumberFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditCardByCardNumberFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditCardByCardNumberFallback(String id, WebClientRequestException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // updateCreditCardFallback
    private Mono<CustomerResponse> updateCreditCardFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> updateCreditCardFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> updateCreditCardFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> updateCreditCardFallback(String id, WebClientRequestException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getCreditCardsByCustomerIdFallback
    private Flux<CustomerResponse> getCreditCardsByCustomerIdFallback(String id, InternalServerErrorException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<CustomerResponse> getCreditCardsByCustomerIdFallback(String id, TimeoutException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<CustomerResponse> getCreditCardsByCustomerIdFallback(String id, CallNotPermittedException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<CustomerResponse> getCreditCardsByCustomerIdFallback(String id, WebClientRequestException e) {
        return Flux.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getCreditCardByIdFallback
    private Mono<CustomerResponse> getCreditCardByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditCardByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditCardByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<CustomerResponse> getCreditCardByIdFallback(String id, WebClientRequestException e) {
        return Mono.error(new BadRequestException(CREDIT_SERVICE_UNAVAILABLE_MESSAGE));
    }

}
