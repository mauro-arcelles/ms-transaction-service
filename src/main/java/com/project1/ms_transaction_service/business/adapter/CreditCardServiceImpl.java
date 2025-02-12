package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CreditCardServiceImpl implements CreditCardService {

    @Autowired
    @Qualifier("creditCardWebClient")
    private WebClient creditCardWebClient;

    @Override
    public Mono<CreditCardResponse> getCreditCardByCardNumber(String cardNumber) {
        return creditCardWebClient.get()
                .uri("/credit-card/by-card-number/{cardNumber}", cardNumber)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToMono(CreditCardResponse.class);
    }

    @Override
    public Mono<CreditCardResponse> updateCreditCard(String id, CreditCardPatchRequest request) {
        return creditCardWebClient.patch()
                .uri("/credit-card/{id}", id)
                .body(Mono.just(request), CreditCardPatchRequest.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToMono(CreditCardResponse.class);
    }

    @Override
    public Flux<CreditCardResponse> getCreditCardsByCustomerId(String customerId) {
        return creditCardWebClient.get()
                .uri("/credit-card/by-customer/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToFlux(CreditCardResponse.class);
    }

}
