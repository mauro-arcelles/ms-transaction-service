package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.CreditPatchRequest;
import com.project1.ms_transaction_service.model.CreditResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {
    Mono<CreditResponse> getCreditById(String creditId);
    Mono<CreditResponse> updateCreditById(String creditId, CreditPatchRequest request);
    Flux<CreditResponse> getCreditsByCustomerId(String customerId);
}
