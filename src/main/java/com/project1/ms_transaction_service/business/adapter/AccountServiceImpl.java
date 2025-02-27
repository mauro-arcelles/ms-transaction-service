package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.InternalServerErrorException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
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
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class AccountServiceImpl implements AccountService {

    final String ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE = "Account service unavailable. Retry again later";

    @Autowired
    @Qualifier("accountWebClient")
    private WebClient accountWebClient;

    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountByAccountNumberFallback")
    @TimeLimiter(name = "accountService")
    @Override
    public Mono<AccountResponse> getAccountByAccountNumber(String accountNumber) {
        return accountWebClient.get()
            .uri("/by-account-number/{accountNumber}", accountNumber)
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
            .bodyToMono(AccountResponse.class);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "updateAccountFallback")
    @TimeLimiter(name = "accountService")
    @Override
    public Mono<AccountResponse> updateAccount(String id, AccountPatchRequest request) {
        return accountWebClient.patch()
            .uri("/{id}", id)
            .body(Mono.just(request), AccountPatchRequest.class)
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
            .bodyToMono(AccountResponse.class);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountsByCustomerIdFallback")
    @TimeLimiter(name = "accountService")
    @Override
    public Flux<AccountResponse> getAccountsByCustomerId(String customerId) {
        return accountWebClient.get()
            .uri("/by-customer/{customerId}", customerId)
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
            .bodyToFlux(AccountResponse.class);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountByIdFallback")
    @TimeLimiter(name = "accountService")
    @Override
    public Mono<AccountResponse> getAccountById(String accountId) {
        return accountWebClient.get()
            .uri("/{accountId}", accountId)
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
            .bodyToMono(AccountResponse.class);
    }

    // getAccountByAccountNumberFallback
    private Mono<AccountResponse> getAccountByAccountNumberFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getAccountByAccountNumberFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getAccountByAccountNumberFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getAccountByAccountNumberFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // updateAccountFallback
    private Mono<AccountResponse> updateAccountFallback(String id, AccountPatchRequest request, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateAccountFallback(String id, AccountPatchRequest request, TimeoutException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateAccountFallback(String id, AccountPatchRequest request, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> updateAccountFallback(String id, AccountPatchRequest request, WebClientException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getAccountsByCustomerIdFallback
    private Flux<AccountResponse> getAccountsByCustomerIdFallback(String id, InternalServerErrorException e) {
        return Flux.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<AccountResponse> getAccountsByCustomerIdFallback(String id, TimeoutException e) {
        return Flux.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<AccountResponse> getAccountsByCustomerIdFallback(String id, CallNotPermittedException e) {
        return Flux.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Flux<AccountResponse> getAccountsByCustomerIdFallback(String id, WebClientException e) {
        return Flux.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    // getAccountByIdFallback
    private Mono<AccountResponse> getAccountByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getAccountByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getAccountByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }

    private Mono<AccountResponse> getAccountByIdFallback(String id, WebClientException e) {
        return Mono.error(new BadRequestException(ACCOUNT_SERVICE_UNAVAILABLE_MESSAGE));
    }
}
