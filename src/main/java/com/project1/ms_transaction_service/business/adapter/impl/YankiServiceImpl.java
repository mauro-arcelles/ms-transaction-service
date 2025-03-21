package com.project1.ms_transaction_service.business.adapter.impl;

import com.project1.ms_transaction_service.business.adapter.YankiService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.InternalServerErrorException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.GetYankiWalletResponse;
import com.project1.ms_transaction_service.model.ResponseBase;
import com.project1.ms_transaction_service.model.UpdateYankiWalletRequest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class YankiServiceImpl implements YankiService {
    final String YANKI_SERVICE_UNAVAILABLE_MESSAGE = "Yanki service unavailable. Retry again later";

    @Autowired
    @Qualifier("yankiWebClient")
    private WebClient yankiWebClient;

    @CircuitBreaker(name = "yankiService", fallbackMethod = "getYankiWalletFallback")
    @TimeLimiter(name = "yankiService")
    @Override
    public Mono<GetYankiWalletResponse> getYankiWallet(String id) {
        return yankiWebClient.get()
            .uri("/wallets/{id}", id)
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
            .bodyToMono(GetYankiWalletResponse.class)
            .doOnNext(response -> log.info("Raw response: {}", response))
            .doOnError(error -> log.error("Error: {}", error.getMessage()))
            .doOnSuccess(success -> log.info("[getYankiWallet] Success!"));
    }

    @CircuitBreaker(name = "yankiService", fallbackMethod = "getYankiWalletByUserIdFallback")
    @TimeLimiter(name = "yankiService")
    @Override
    public Mono<GetYankiWalletResponse> getYankiWalletByUserId(String userId) {
        return yankiWebClient.get()
            .uri("/wallets/by-user-id/{userId}", userId)
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
            .bodyToMono(GetYankiWalletResponse.class)
            .doOnNext(response -> log.info("Raw response: {}", response))
            .doOnError(error -> log.error("Error: {}", error.getMessage()))
            .doOnSuccess(success -> log.info("[getYankiWallet] Success!"));
    }

    @CircuitBreaker(name = "yankiService", fallbackMethod = "updateYankiWalletFallback")
    @TimeLimiter(name = "yankiService")
    @Override
    public Mono<Void> updateYankiWallet(String id, UpdateYankiWalletRequest request) {
        return yankiWebClient.put()
            .uri("/wallets/{id}", id)
            .body(Mono.just(request), UpdateYankiWalletRequest.class)
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
            .bodyToMono(Void.class);
    }

    // getYankiWalletFallback
    private Mono<GetYankiWalletResponse> getYankiWalletFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<GetYankiWalletResponse> getYankiWalletFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<GetYankiWalletResponse> getYankiWalletFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<GetYankiWalletResponse> getYankiWalletFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // updateYankiWalletFallback
    private Mono<Void> updateYankiWalletFallback(String id, UpdateYankiWalletRequest request, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<Void> updateYankiWalletFallback(String id, UpdateYankiWalletRequest request, TimeoutException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<Void> updateYankiWalletFallback(String id, UpdateYankiWalletRequest request, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<Void> updateYankiWalletFallback(String id, UpdateYankiWalletRequest request, WebClientException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getYankiWalletByUserIdFallback
    private Mono<GetYankiWalletResponse> getYankiWalletByUserIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<GetYankiWalletResponse> getYankiWalletByUserIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<GetYankiWalletResponse> getYankiWalletByUserIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<GetYankiWalletResponse> getYankiWalletByUserIdFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(YANKI_SERVICE_UNAVAILABLE_MESSAGE));
    }
}
