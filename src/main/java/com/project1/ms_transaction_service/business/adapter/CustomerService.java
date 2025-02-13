package com.project1.ms_transaction_service.business.adapter;

import com.project1.ms_transaction_service.model.CustomerResponse;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerResponse> getCustomerByDni(String dni);
    Mono<CustomerResponse> getCustomerByRuc(String ruc);
    Mono<CustomerResponse> getCustomerById(String id);
}
