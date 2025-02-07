package com.project1.ms_transaction_service;

import com.project1.ms_transaction_service.api.TransactionsApiDelegate;
import com.project1.ms_transaction_service.business.TransactionService;
import com.project1.ms_transaction_service.model.TransactionRequest;
import com.project1.ms_transaction_service.model.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Component
public class TransactionApiDelegateImpl implements TransactionsApiDelegate {

    @Autowired
    private TransactionService transactionService;

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> transactionsAccountIdGet(String accountId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> transactionsPost(@Valid Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        return transactionService.createTransaction(transactionRequest)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));

    }
}
