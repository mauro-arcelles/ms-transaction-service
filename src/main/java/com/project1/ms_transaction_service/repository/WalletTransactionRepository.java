package com.project1.ms_transaction_service.repository;

import com.project1.ms_transaction_service.model.entity.WalletTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends ReactiveMongoRepository<WalletTransaction, String> {
}
