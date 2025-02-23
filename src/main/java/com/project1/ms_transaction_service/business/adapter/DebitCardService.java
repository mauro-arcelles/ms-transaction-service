package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.DebitCardResponse;
import reactor.core.publisher.Mono;

public interface DebitCardService {
    Mono<DebitCardResponse> getDebitCardById(String debitCardId);
}
