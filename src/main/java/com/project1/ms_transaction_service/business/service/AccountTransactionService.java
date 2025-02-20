package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountTransactionService {
    Mono<AccountTransactionResponse> createAccountTransaction(Mono<AccountTransactionRequest> request);

    Flux<AccountTransactionResponse> getTransactionsByAccountNumber(String accountNumber);
}
