package com.project1.ms_transaction_service.business.service;

import reactor.core.publisher.Mono;

public interface BootcoinTransactionService {
    Mono<Void> processBootcoinTransaction(String transactionId);
}
