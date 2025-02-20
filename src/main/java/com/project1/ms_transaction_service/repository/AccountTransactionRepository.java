package com.project1.ms_transaction_service.repository;

import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.model.entity.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface AccountTransactionRepository extends ReactiveMongoRepository<AccountTransaction, String> {
    Flux<AccountTransaction> findAllByDestinationAccountNumber(String accountNumber);

    Flux<AccountTransaction> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
