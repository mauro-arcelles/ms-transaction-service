package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.model.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {
    Mono<CustomerProductsResponse> getAllCustomerProductsByDni(String dni);

    Mono<CustomerProductsResponse> getAllCustomerProductsByRuc(String ruc);

    Mono<CustomerProductsResponse> getAllCustomerProductsByCustomerId(String customerId);

    Mono<CustomerProductsAverageBalanceResponse> getAllCustomerProductsAvgBalanceCustomerId(String customerId);

    Mono<ProductsCommissionResponse> getProductsCommissionByRange(LocalDateTime startDate, LocalDateTime endDate);
}
