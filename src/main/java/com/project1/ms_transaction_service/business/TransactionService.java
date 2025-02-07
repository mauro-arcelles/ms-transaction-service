package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.TransactionRequest;
import com.project1.ms_transaction_service.model.TransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<TransactionResponse> createTransaction(Mono<TransactionRequest> request);
    Flux<TransactionResponse> getTransaccionsByAccountNumber(String accountNumber);
}
