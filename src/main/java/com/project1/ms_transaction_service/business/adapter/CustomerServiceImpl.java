package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.CustomerResponse;
import com.project1.ms_transaction_service.model.ResponseBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    @Qualifier("customerWebClient")
    private WebClient webClient;

    @Override
    public Mono<CustomerResponse> getCustomerByDni(String dni) {
        return webClient.get()
                .uri("/dni/{dni}", dni)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error -> Mono.error(new BadRequestException(error.getMessage()))))
                .bodyToMono(CustomerResponse.class);
    }

    @Override
    public Mono<CustomerResponse> getCustomerByRuc(String ruc) {
        return webClient.get()
                .uri("/ruc/{ruc}", ruc)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error -> Mono.error(new BadRequestException(error.getMessage()))))
                .bodyToMono(CustomerResponse.class);
    }

    @Override
    public Mono<CustomerResponse> getCustomerById(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error -> Mono.error(new BadRequestException(error.getMessage()))))
                .bodyToMono(CustomerResponse.class);
    }
}
