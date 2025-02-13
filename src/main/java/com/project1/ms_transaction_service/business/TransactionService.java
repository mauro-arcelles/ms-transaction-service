package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.*;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<CustomerProductsResponse> getAllCustomerProductsByDni(String dni);
    Mono<CustomerProductsResponse> getAllCustomerProductsByRuc(String ruc);
}
