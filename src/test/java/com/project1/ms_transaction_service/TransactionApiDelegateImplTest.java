package com.project1.ms_transaction_service;

import com.project1.ms_transaction_service.business.service.*;
import com.project1.ms_transaction_service.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionApiDelegateImplTest {

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AccountTransactionService accountTransactionService;

    @MockBean
    private CreditCardTransactionService creditCardTransactionService;

    @MockBean
    private CreditTransactionService creditTransactionService;

    @MockBean
    private DebitCardTransactionService debitCardTransactionService;

    @Autowired
    private TransactionApiDelegateImpl transactionApiDelegate;

    @Test
    void getAccountTransactionsByAccountNumber() {
        String accountNumber = "123";
        Flux<AccountTransactionResponse> response = Flux.just(new AccountTransactionResponse());

        when(accountTransactionService.getTransactionsByAccountNumber(accountNumber))
            .thenReturn(response);

        StepVerifier.create(transactionApiDelegate.getAccountTransactionsByAccountNumber(accountNumber, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void getCreditCardTransactionsByCreditCardId() {
        String creditCardId = "123";
        Flux<CreditCardTransactionResponse> response = Flux.just(new CreditCardTransactionResponse());

        when(creditCardTransactionService.getCreditCardTransactionsById(creditCardId))
            .thenReturn(response);

        StepVerifier.create(transactionApiDelegate.getCreditCardTransactionsByCreditCardId(creditCardId, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void getCreditTransactionsByCreditId() {
        String creditCardId = "123";
        Flux<CreditPaymentTransactionResponse> response = Flux.just(new CreditPaymentTransactionResponse());

        when(creditTransactionService.getCreditTransactionsByCreditId(creditCardId))
            .thenReturn(response);

        StepVerifier.create(transactionApiDelegate.getCreditTransactionsByCreditId(creditCardId, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void createCreditCardTransaction() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        CreditCardTransactionResponse response = new CreditCardTransactionResponse();

        when(creditCardTransactionService.createCreditCardTransaction(any()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.createCreditCardTransaction(Mono.just(request), null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.CREATED)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsByRuc() {
        String ruc = "123";

        CustomerProductsResponse response = new CustomerProductsResponse();

        when(transactionService.getAllCustomerProductsByRuc(any()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.getAllCustomerProductsByRuc(ruc, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void createTransactionAccounts() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountTransactionService.createAccountTransaction(any()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.createTransactionAccounts(Mono.just(request), null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.CREATED)
            .verifyComplete();
    }

    @Test
    void createCreditPaymentTransaction() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        CreditPaymentTransactionResponse response = new CreditPaymentTransactionResponse();

        when(creditTransactionService.createCreditPaymentTransaction(any()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.createCreditPaymentTransaction(Mono.just(request), null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.CREATED)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsByCustomerId() {
        String customerId = "123";
        CustomerProductsResponse response = new CustomerProductsResponse();

        when(transactionService.getAllCustomerProductsByCustomerId(any()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.getAllCustomerProductsByCustomerId(customerId, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsAvgBalanceByCustomerId() {
        String customerId = "123";
        CustomerProductsAverageBalanceResponse response = new CustomerProductsAverageBalanceResponse();

        when(transactionService.getAllCustomerProductsAvgBalanceCustomerId(customerId))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.getAllCustomerProductsAvgBalanceByCustomerId(customerId, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsByDni() {
        String dni = "12345678";
        CustomerProductsResponse response = new CustomerProductsResponse();

        when(transactionService.getAllCustomerProductsByDni(dni))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.getAllCustomerProductsByDni(dni, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void createDebitCardTransaction() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        DebitCardTransactionResponse response = new DebitCardTransactionResponse();

        when(debitCardTransactionService.createDebitCardTransaction(any()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.createDebitCardTransaction(Mono.just(request), null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.CREATED)
            .verifyComplete();
    }

    @Test
    void getProductsCommissionRange() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        ProductsCommissionResponse response = new ProductsCommissionResponse();

        when(transactionService.getProductsCommissionByRange(startDate, endDate))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.getAllProductsCommissionRange(startDate, endDate, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }

    @Test
    void getCreditAndDebitTransactionsLimit() {
        String creditCardId = "123";
        String debitCardId = "234";
        String limit = "10";
        CustomerProductsCreditDebitCardsTransactionsResponse response = new CustomerProductsCreditDebitCardsTransactionsResponse();

        when(transactionService.getCreditDebitCardTransactionsLimit(creditCardId, debitCardId, limit))
            .thenReturn(Mono.just(response));

        StepVerifier.create(transactionApiDelegate.getCreditAndDebitTransactionsLimit(creditCardId, debitCardId, limit, null))
            .expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK)
            .verifyComplete();
    }
}
