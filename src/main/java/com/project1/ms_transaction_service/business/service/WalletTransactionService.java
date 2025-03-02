package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import reactor.core.publisher.Mono;

public interface WalletTransactionService {
    Mono<Void> createWalletTransaction(Mono<CreateWalletTransactionRequest> request);
}
