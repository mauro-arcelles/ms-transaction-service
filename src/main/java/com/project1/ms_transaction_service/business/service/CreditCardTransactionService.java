package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardTransactionService {
    Mono<CreditCardTransactionResponse> createCreditCardUsageTransaction(Mono<CreditCardTransactionRequest> request);

    Mono<CreditCardTransactionResponse> createCreditCardPaymentTransaction(Mono<CreditCardTransactionRequest> request);

    Flux<CreditCardTransactionResponse> getCreditCardTransactionsByCardNumber(String originAccountNumber);
}
