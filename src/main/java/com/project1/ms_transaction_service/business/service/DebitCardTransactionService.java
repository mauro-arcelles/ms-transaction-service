package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.model.DebitCardTransactionRequest;
import com.project1.ms_transaction_service.model.DebitCardTransactionResponse;
import reactor.core.publisher.Mono;

public interface DebitCardTransactionService {
    Mono<DebitCardTransactionResponse> createDebitCardTransaction(Mono<DebitCardTransactionRequest> request);
}
