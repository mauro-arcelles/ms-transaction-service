package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.ResponseBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    @Qualifier("accountWebClient")
    private WebClient accountWebClient;

    @Override
    public Mono<AccountResponse> getAccountByAccountNumber(String accountNumber) {
        return accountWebClient.get()
                .uri("/by-account-number/{accountNumber}", accountNumber)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToMono(AccountResponse.class);
    }

    @Override
    public Mono<AccountResponse> updateAccount(String id, AccountPatchRequest request) {
        return accountWebClient.patch()
                .uri("/{id}", id)
                .body(Mono.just(request), AccountPatchRequest.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToMono(AccountResponse.class);
    }

    @Override
    public Flux<AccountResponse> getAccountsByCustomerId(String customerId) {
        return accountWebClient.get()
                .uri("/by-customer/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToFlux(AccountResponse.class);
    }
}
