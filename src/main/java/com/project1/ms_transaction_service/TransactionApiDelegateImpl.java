package com.project1.ms_transaction_service;

import com.project1.ms_transaction_service.api.TransactionsApiDelegate;
import com.project1.ms_transaction_service.business.TransactionService;
import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TransactionApiDelegateImpl implements TransactionsApiDelegate {

    @Autowired
    private TransactionService transactionService;

    @Override
    public Mono<ResponseEntity<Flux<AccountTransactionResponse>>> getTransactionsByAccount(String accountNumber, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok().body(transactionService.getTransaccionsByAccountNumber(accountNumber)));
    }

    @Override
    public Mono<ResponseEntity<AccountTransactionResponse>> createTransactionAccounts(Mono<AccountTransactionRequest> accountTransactionRequest, ServerWebExchange exchange) {
        return transactionService.createAccountTransaction(accountTransactionRequest)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<CreditCardTransactionResponse>> createTransactionCreditCard(Mono<CreditCardTransactionRequest> creditCardTransactionRequest, ServerWebExchange exchange) {
        return transactionService.createCreditCardTransaction(creditCardTransactionRequest).map(ResponseEntity::ok);
    }
}
