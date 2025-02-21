package com.project1.ms_transaction_service.repository;

import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CreditCardTransactionRepository extends ReactiveMongoRepository<CreditCardTransaction, String> {
    Flux<CreditCardTransaction> findAllByCreditCard(String creditCard);
}
