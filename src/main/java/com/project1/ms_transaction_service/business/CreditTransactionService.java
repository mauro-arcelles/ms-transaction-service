package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.CreditPaymentTransactionRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionResponse;
import reactor.core.publisher.Mono;

public interface CreditTransactionService {
    Mono<CreditPaymentTransactionResponse> createCreditPaymentTransaction(Mono<CreditPaymentTransactionRequest> request);
}
