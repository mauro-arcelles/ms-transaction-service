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
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class DebitCardServiceImpl implements DebitCardService {

    final String ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE = "Account service unavailable. Retry again later";

    @Autowired
    @Qualifier("accountWebClient")
    private WebClient accountWebClient;

    @CircuitBreaker(name = "accountService", fallbackMethod = "getDebitCardByIdFallback")
    @TimeLimiter(name = "accountService")
    @Override
    public Mono<DebitCardResponse> getDebitCardById(String debitCardId) {
        return accountWebClient.get()
            .uri("/debit-card/{debitCardId}", debitCardId)
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
            .bodyToMono(DebitCardResponse.class);
    }

    // getDebitCardByIdFallback
    private Mono<DebitCardResponse> getDebitCardByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<DebitCardResponse> getDebitCardByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<DebitCardResponse> getDebitCardByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<DebitCardResponse> getDebitCardByIdFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }
}
