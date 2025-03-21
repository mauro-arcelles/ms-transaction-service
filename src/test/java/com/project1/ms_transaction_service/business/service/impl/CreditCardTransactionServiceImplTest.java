package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.business.mapper.CreditCardTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.CreditCardCustomerMismatchException;
import com.project1.ms_transaction_service.model.CreditCardResponse;
import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CustomerResponse;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.CreditCardTransactionType;
import com.project1.ms_transaction_service.repository.CreditCardTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest
class CreditCardTransactionServiceImplTest {

    @Autowired
    private CreditCardTransactionServiceImpl creditCardTransactionService;

    @Autowired
    private CreditCardTransactionMapper creditCardTransactionMapper;

    @MockBean
    private CreditCardService creditCardService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CreditCardTransactionRepository creditCardTransactionRepository;

    @Test
    void createCreditCardTransaction_Usage_Success() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setType("USAGE");
        request.setAmount(new BigDecimal("100"));
        request.setCreditCardId("123");
        request.setCustomerId("456");

        CreditCardResponse card = new CreditCardResponse();
        card.setId("123");
        card.setCustomerId("456");
        card.setCreditLimit(new BigDecimal("1000"));
        card.setUsedAmount(new BigDecimal("0"));

        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setAmount(new BigDecimal("100"));
        transaction.setType(CreditCardTransactionType.USAGE);

        when(creditCardService.getCreditCardById("123")).thenReturn(Mono.just(card));
        when(creditCardTransactionRepository.save(any())).thenReturn(Mono.just(transaction));
        when(creditCardService.updateCreditCard(anyString(), any())).thenReturn(Mono.just(card));

        StepVerifier.create(creditCardTransactionService.createCreditCardTransaction(Mono.just(request)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void createCreditCardTransaction_Payment_Success() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setType("PAYMENT");
        request.setAmount(new BigDecimal("50"));
        request.setCreditCardId("123");
        request.setCustomerId("456");

        CreditCardResponse card = new CreditCardResponse();
        card.setId("123");
        card.setUsedAmount(new BigDecimal("100"));

        CustomerResponse customer = new CustomerResponse();
        customer.setId("456");

        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setAmount(new BigDecimal("50"));
        transaction.setType(CreditCardTransactionType.PAYMENT);

        when(creditCardService.getCreditCardById("123")).thenReturn(Mono.just(card));
        when(customerService.getCustomerById("456")).thenReturn(Mono.just(customer));
        when(creditCardTransactionRepository.save(any())).thenReturn(Mono.just(transaction));
        when(creditCardService.updateCreditCard(anyString(), any())).thenReturn(Mono.just(card));

        StepVerifier.create(creditCardTransactionService.createCreditCardTransaction(Mono.just(request)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getCreditCardTransactionsById_Success() {
        String cardId = "123";
        CreditCardResponse card = new CreditCardResponse();
        card.setId(cardId);

        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setCreditCardId(cardId);
        transaction.setType(CreditCardTransactionType.USAGE);

        when(creditCardService.getCreditCardById(cardId)).thenReturn(Mono.just(card));
        when(creditCardTransactionRepository.findAllByCreditCardId(cardId)).thenReturn(Flux.just(transaction));

        StepVerifier.create(creditCardTransactionService.getCreditCardTransactionsById(cardId))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void createCreditCardTransaction_InsufficientFunds() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setType("USAGE");
        request.setAmount(new BigDecimal("2000"));
        request.setCreditCardId("123");
        request.setCustomerId("456");

        CreditCardResponse card = new CreditCardResponse();
        card.setId("123");
        card.setCustomerId("456");
        card.setCreditLimit(new BigDecimal("1000"));
        card.setUsedAmount(new BigDecimal("0"));

        when(creditCardService.getCreditCardById("123")).thenReturn(Mono.just(card));

        StepVerifier.create(creditCardTransactionService.createCreditCardTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void validateCustomer_UsageTransaction_CustomerMismatch() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setType("USAGE");
        request.setAmount(new BigDecimal("100"));
        request.setCreditCardId("123");
        request.setCustomerId("456"); // Different from card owner

        CreditCardResponse card = new CreditCardResponse();
        card.setId("123");
        card.setCustomerId("789"); // Different customer ID
        card.setCreditLimit(new BigDecimal("1000"));
        card.setUsedAmount(new BigDecimal("0"));

        when(creditCardService.getCreditCardById("123")).thenReturn(Mono.just(card));

        StepVerifier.create(creditCardTransactionService.createCreditCardTransaction(Mono.just(request)))
            .expectError(CreditCardCustomerMismatchException.class)
            .verify();
    }

    @Test
    void validateCreditCardUsageLimit_Payment_AmountExceedsUsedAmount() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setType("PAYMENT");
        request.setAmount(new BigDecimal("200")); // Payment amount larger than used amount
        request.setCreditCardId("123");
        request.setCustomerId("456");

        CreditCardResponse card = new CreditCardResponse();
        card.setId("123");
        card.setCustomerId("456");
        card.setCreditLimit(new BigDecimal("1000"));
        card.setUsedAmount(new BigDecimal("100"));

        when(creditCardService.getCreditCardById("123")).thenReturn(Mono.just(card));
        when(customerService.getCustomerById("456")).thenReturn(Mono.just(new CustomerResponse()));

        StepVerifier.create(creditCardTransactionService.createCreditCardTransaction(Mono.just(request)))
            .expectErrorMatches(throwable ->
                throwable instanceof BadRequestException &&
                    throwable.getMessage().equals("Cannot complete the transaction. Amount to pay is more than the actual CREDIT CARD used amount"))
            .verify();
    }
}
