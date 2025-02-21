package com.project1.ms_transaction_service.repository;

import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CreditTransactionRepository extends ReactiveMongoRepository<CreditTransaction, String> {
    Flux<CreditTransaction> findAllByCreditId(String creditId);
}
