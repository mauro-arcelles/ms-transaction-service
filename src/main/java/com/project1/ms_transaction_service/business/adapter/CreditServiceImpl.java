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
public class CreditServiceImpl implements CreditService {

    @Autowired
    @Qualifier("creditWebClient")
    private WebClient creditWebClient;

    @Override
    public Mono<CreditResponse> getCreditById(String creditId) {
        return creditWebClient.get()
                .uri("/credit/{creditId}", creditId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToMono(CreditResponse.class);
    }

    @Override
    public Mono<CreditResponse> updateCreditById(String creditId, CreditPatchRequest request) {
        return creditWebClient.patch()
                .uri("/credit/{creditId}", creditId)
                .body(Mono.just(request), CreditPatchRequest.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToMono(CreditResponse.class);
    }

    @Override
    public Flux<CreditResponse> getCreditsByCustomerId(String customerId) {
        return creditWebClient.get()
                .uri("/credit/by-customer/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToFlux(CreditResponse.class);
    }
}
