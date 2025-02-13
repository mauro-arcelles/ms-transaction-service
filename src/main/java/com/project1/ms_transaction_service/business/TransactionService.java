package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<AccountTransactionResponse> createAccountTransaction(Mono<AccountTransactionRequest> request);
    Flux<AccountTransactionResponse> getTransactionsByAccountNumber(String accountNumber);
    Mono<CreditCardUsageTransactionResponse> createCreditCardUsageTransaction(Mono<CreditCardUsageTransactionRequest> request);
    Mono<CustomerProductsResponse> getAllCustomerProductsByDni(String dni);
    Mono<CustomerProductsResponse> getAllCustomerProductsByRuc(String ruc);
    Mono<CreditPaymentTransactionResponse> createCreditPaymentTransaction(Mono<CreditPaymentTransactionRequest> request);
}
