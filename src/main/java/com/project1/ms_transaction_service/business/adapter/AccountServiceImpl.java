package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.AccountWebClientException;
import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.ResponseBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    @Qualifier("accountWebClient")
    private WebClient accountWebClient;

    @Override
    public Mono<AccountResponse> findAccountByAccountNumber(String accountNumber) {
        return accountWebClient.get()
                .uri("/by-account-number/{accountNumber}", accountNumber)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new AccountWebClientException(error.getMessage()))
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
                                        Mono.error(new AccountWebClientException(error.getMessage()))
                                )
                )
                .bodyToMono(AccountResponse.class);
    }
}
