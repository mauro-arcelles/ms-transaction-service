package com.project1.ms_transaction_service.repository;

import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface DebitCardTransactionRepository extends ReactiveMongoRepository<DebitCardTransaction, String> {
    Flux<DebitCardTransaction> findAllByDebitCardIdOrderByDateDesc(String debitCardId, Pageable pageable);
}
