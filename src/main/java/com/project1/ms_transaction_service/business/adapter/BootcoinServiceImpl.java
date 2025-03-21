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
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class BootcoinServiceImpl implements BootcoinService {
    final String BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE = "Bootcoin service unavailable. Retry again later";

    @Autowired
    @Qualifier("bootcoinWebClient")
    private WebClient bootcoinWebClient;

    @CircuitBreaker(name = "bootcoinService", fallbackMethod = "getExchangeRequestByTransactionIdFallback")
    @TimeLimiter(name = "bootcoinService")
    @Override
    public Mono<GetExchangeRequestByTransactionIdResponse> getExchangeRequestByTransactionId(String transactionId) {
        return bootcoinWebClient.get()
            .uri("/exchange/requests/by-transaction-id/{transactionId}", transactionId)
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
            .bodyToMono(GetExchangeRequestByTransactionIdResponse.class);
    }

    @CircuitBreaker(name = "bootcoinService", fallbackMethod = "getBootcoinWalletByUserIdFallback")
    @TimeLimiter(name = "bootcoinService")
    @Override
    public Mono<CreateBootcoinWalletResponse> getBootcoinWalletByUserId(String userId) {
        return bootcoinWebClient.get()
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
            .bodyToMono(CreateBootcoinWalletResponse.class);
    }

    @CircuitBreaker(name = "bootcoinService", fallbackMethod = "updateBootcoinWalletFallback")
    @TimeLimiter(name = "bootcoinService")
    @Override
    public Mono<Void> updateBootcoinWallet(String walletId, UpdateBootcoinWalletRequest request) {
        return bootcoinWebClient.put()
            .uri("/wallets/{walletId}", walletId)
            .body(Mono.just(request), UpdateBootcoinWalletRequest.class)
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

    @CircuitBreaker(name = "bootcoinService", fallbackMethod = "updateExchangeRequestFallback")
    @TimeLimiter(name = "bootcoinService")
    @Override
    public Mono<Void> updateExchangeRequest(String exchangeRequestId, UpdateExchangeRequestRequest request) {
        return bootcoinWebClient.put()
            .uri("/exchange/requests/{exchangeRequestId}", exchangeRequestId)
            .body(Mono.just(request), UpdateExchangeRequestRequest.class)
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

    // getExchangeRequestByTransactionIdFallback
    private Mono<AccountResponse> getExchangeRequestByTransactionIdFallback(String transactionId, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getExchangeRequestByTransactionIdFallback(String transactionId, TimeoutException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getExchangeRequestByTransactionIdFallback(String transactionId, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getExchangeRequestByTransactionIdFallback(String transactionId, WebClientException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getBootcoinWalletByUserIdFallback
    private Mono<AccountResponse> getBootcoinWalletByUserIdFallback(String userId, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getBootcoinWalletByUserIdFallback(String userId, TimeoutException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getBootcoinWalletByUserIdFallback(String userId, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getBootcoinWalletByUserIdFallback(String userId, WebClientException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // updateBootcoinWalletFallback
    private Mono<AccountResponse> updateBootcoinWalletFallback(String walletId, UpdateBootcoinWalletRequest request, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateBootcoinWalletFallback(String walletId, UpdateBootcoinWalletRequest request, TimeoutException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateBootcoinWalletFallback(String walletId, UpdateBootcoinWalletRequest request, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateBootcoinWalletFallback(String walletId, UpdateBootcoinWalletRequest request, WebClientException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // updateExchangeRequestFallback
    private Mono<AccountResponse> updateExchangeRequestFallback(String exchangeRequestId, UpdateExchangeRequestRequest request,
                                                                InternalServerErrorException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateExchangeRequestFallback(String exchangeRequestId, UpdateExchangeRequestRequest request, TimeoutException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateExchangeRequestFallback(String exchangeRequestId, UpdateExchangeRequestRequest request, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateExchangeRequestFallback(String exchangeRequestId, UpdateExchangeRequestRequest request, WebClientException e) {
        return Mono.error(new BadRequestException(BOOTCOIN_SERVICE_UNAVAILABLE_MESSAGE));
    }
}
