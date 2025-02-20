package com.project1.ms_transaction_service;

import com.project1.ms_transaction_service.api.TransactionsApiDelegate;
import com.project1.ms_transaction_service.business.service.AccountTransactionService;
import com.project1.ms_transaction_service.business.service.CreditCardTransactionService;
import com.project1.ms_transaction_service.business.service.CreditTransactionService;
import com.project1.ms_transaction_service.business.service.TransactionService;
import com.project1.ms_transaction_service.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class TransactionApiDelegateImpl implements TransactionsApiDelegate {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Autowired
    private CreditCardTransactionService creditCardTransactionService;

    @Autowired
    private CreditTransactionService creditTransactionService;

    @Override
    public Mono<ResponseEntity<Flux<AccountTransactionResponse>>> getTransactionsByAccount(String accountNumber, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok().body(accountTransactionService.getTransactionsByAccountNumber(accountNumber)));
    }

    @Override
    public Mono<ResponseEntity<AccountTransactionResponse>> createTransactionAccounts(Mono<AccountTransactionRequest> accountTransactionRequest,
                                                                                      ServerWebExchange exchange) {
        return accountTransactionService.createAccountTransaction(accountTransactionRequest)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<CreditCardUsageTransactionResponse>> createCreditCardUsageTransaction(
        Mono<CreditCardUsageTransactionRequest> creditCardTransactionRequest, ServerWebExchange exchange) {
        return creditCardTransactionService.createCreditCardUsageTransaction(creditCardTransactionRequest).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerProductsResponse>> getAllCustomerProductsByDni(String dni, ServerWebExchange exchange) {
        return transactionService.getAllCustomerProductsByDni(dni).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerProductsResponse>> getAllCustomerProductsByRuc(String ruc, ServerWebExchange exchange) {
        return transactionService.getAllCustomerProductsByRuc(ruc).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CreditPaymentTransactionResponse>> createCreditPaymentTransaction(
        Mono<CreditPaymentTransactionRequest> creditPaymentTransactionRequest, ServerWebExchange exchange) {
        return creditTransactionService.createCreditPaymentTransaction(creditPaymentTransactionRequest).map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<CustomerProductsResponse>> getAllCustomerProductsByCustomerId(String customerId, ServerWebExchange exchange) {
        return transactionService.getAllCustomerProductsByCustomerId(customerId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerProductsAverageBalanceResponse>> getAllCustomerProductsAvgBalanceByCustomerId(String customerId,
                                                                                                                     ServerWebExchange exchange) {
        return transactionService.getAllCustomerProductsAvgBalanceCustomerId(customerId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ProductsCommissionResponse>> getAllProductsCommissionRange(LocalDateTime startDate, LocalDateTime endDate,
                                                                                          ServerWebExchange exchange) {
        return transactionService.getProductsCommissionByRange(startDate, endDate).map(ResponseEntity::ok);
    }
}
