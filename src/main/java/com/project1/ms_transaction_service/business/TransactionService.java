package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<AccountTransactionResponse> createAccountTransaction(Mono<AccountTransactionRequest> request);
    Flux<AccountTransactionResponse> getTransactionsByAccountNumber(String accountNumber);
    Mono<CreditCardTransactionResponse> createCreditCardTransaction(Mono<CreditCardTransactionRequest> request);
}
