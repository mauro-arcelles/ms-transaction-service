package com.project1.ms_transaction_service.repository;

import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface DebitCardTransactionRepository extends ReactiveMongoRepository<DebitCardTransaction, String> {
}
