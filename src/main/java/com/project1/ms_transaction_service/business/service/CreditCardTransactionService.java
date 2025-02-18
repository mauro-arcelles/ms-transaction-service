package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.model.CreditCardUsageTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardUsageTransactionResponse;
import reactor.core.publisher.Mono;

public interface CreditCardTransactionService {
    Mono<CreditCardUsageTransactionResponse> createCreditCardUsageTransaction(Mono<CreditCardUsageTransactionRequest> request);
}
