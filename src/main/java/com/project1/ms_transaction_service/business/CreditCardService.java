package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.CreditCardPatchRequest;
import com.project1.ms_transaction_service.model.CreditCardResponse;
import reactor.core.publisher.Mono;

public interface CreditCardService {
    Mono<CreditCardResponse> getCreditCardByCardNumber(String cardNumber);
    Mono<CreditCardResponse> updateCreditCard(String id, CreditCardPatchRequest request);
}
