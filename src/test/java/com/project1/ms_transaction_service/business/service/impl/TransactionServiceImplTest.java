package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.business.adapter.CreditService;
import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.business.mapper.TransactionMapper;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.CreditCardResponse;
import com.project1.ms_transaction_service.model.CreditResponse;
import com.project1.ms_transaction_service.model.CustomerResponse;
import com.project1.ms_transaction_service.model.entity.*;
import com.project1.ms_transaction_service.repository.AccountTransactionRepository;
import com.project1.ms_transaction_service.repository.CreditCardTransactionRepository;
import com.project1.ms_transaction_service.repository.DebitCardTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionServiceImplTest {

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private TransactionMapper transactionMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CreditCardService creditCardService;

    @MockBean
    private CreditService creditService;

    @MockBean
    private AccountTransactionRepository accountTransactionRepository;

    @MockBean
    private CreditCardTransactionRepository creditCardTransactionRepository;

    @MockBean
    private DebitCardTransactionRepository debitCardTransactionRepository;

    @Test
    void getAllCustomerProductsByDni_Success() {
        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setDocumentNumber("12345678");

        when(customerService.getCustomerByDni("12345678")).thenReturn(Mono.just(customer));
        when(accountService.getAccountsByCustomerId("123")).thenReturn(Flux.just(new AccountResponse()));
        when(creditCardService.getCreditCardsByCustomerId("123")).thenReturn(Flux.just(new CreditCardResponse()));
        when(creditService.getCreditsByCustomerId("123")).thenReturn(Flux.just(new CreditResponse()));

        StepVerifier.create(transactionService.getAllCustomerProductsByDni("12345678"))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsByRuc_Success() {
        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setRuc("12345678901");

        when(customerService.getCustomerByRuc("12345678901")).thenReturn(Mono.just(customer));
        when(accountService.getAccountsByCustomerId("123")).thenReturn(Flux.just(new AccountResponse()));
        when(creditCardService.getCreditCardsByCustomerId("123")).thenReturn(Flux.just(new CreditCardResponse()));
        when(creditService.getCreditsByCustomerId("123")).thenReturn(Flux.just(new CreditResponse()));

        StepVerifier.create(transactionService.getAllCustomerProductsByRuc("12345678901"))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsByCustomerId_Success() {
        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountService.getAccountsByCustomerId("123")).thenReturn(Flux.just(new AccountResponse()));
        when(creditCardService.getCreditCardsByCustomerId("123")).thenReturn(Flux.just(new CreditCardResponse()));
        when(creditService.getCreditsByCustomerId("123")).thenReturn(Flux.just(new CreditResponse()));

        StepVerifier.create(transactionService.getAllCustomerProductsByCustomerId("123"))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getAllCustomerProductsAvgBalanceCustomerId_Success() {
        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountService.getAccountsByCustomerId("123")).thenReturn(Flux.just(new AccountResponse()));
        when(creditCardService.getCreditCardsByCustomerId("123")).thenReturn(Flux.just(new CreditCardResponse()));
        when(creditService.getCreditsByCustomerId("123")).thenReturn(Flux.just(new CreditResponse()));

        StepVerifier.create(transactionService.getAllCustomerProductsAvgBalanceCustomerId("123"))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getProductsCommissionByRange_Success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<AccountTransaction> transactions = Arrays.asList(new AccountTransaction());

        when(accountTransactionRepository.findAllByDateBetween(startDate, endDate))
            .thenReturn(Flux.fromIterable(transactions));

        StepVerifier.create(transactionService.getProductsCommissionByRange(startDate, endDate))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getCreditDebitCardTransactionsLimit_Success() {
        String creditCardId = "123";
        String debitCardId = "456";
        String limit = "10";

        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setType(CreditCardTransactionType.USAGE);
        List<CreditCardTransaction> creditTransactions = List.of(creditCardTransaction);

        DebitCardTransaction debitCardTransaction = new DebitCardTransaction();
        debitCardTransaction.setType(DebitCardTransactionType.PURCHASE);
        List<DebitCardTransaction> debitTransactions = List.of(debitCardTransaction);

        when(creditCardTransactionRepository.findAllByCreditCardIdOrderByDateDesc(
            eq(creditCardId), any(PageRequest.class)))
            .thenReturn(Flux.fromIterable(creditTransactions));

        when(debitCardTransactionRepository.findAllByDebitCardIdOrderByDateDesc(
            eq(debitCardId), any(PageRequest.class)))
            .thenReturn(Flux.fromIterable(debitTransactions));

        StepVerifier.create(transactionService.getCreditDebitCardTransactionsLimit(creditCardId, debitCardId, limit))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getCreditDebitCardTransactionsLimit_NullIds_Success() {
        String limit = "10";

        StepVerifier.create(transactionService.getCreditDebitCardTransactionsLimit(null, null, limit))
            .expectNextCount(1)
            .verifyComplete();
    }
}
