package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.CreditService;
import com.project1.ms_transaction_service.business.adapter.CustomerService;
import com.project1.ms_transaction_service.business.mapper.CreditTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import com.project1.ms_transaction_service.model.entity.CreditTransactionType;
import com.project1.ms_transaction_service.repository.CreditTransactionRepository;
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
class CreditTransactionServiceImplTest {

    @Autowired
    private CreditTransactionServiceImpl creditTransactionService;

    @MockBean
    private CreditService creditService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CreditTransactionRepository creditTransactionRepository;

    @MockBean
    private CreditTransactionMapper creditTransactionMapper;

    @Test
    void createCreditPaymentTransaction_Success() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        request.setCreditId("123");
        request.setCustomerId("456");

        CreditResponse creditResponse = new CreditResponse();
        creditResponse.setAmountPaid(BigDecimal.valueOf(100));
        creditResponse.setMonthlyPayment(BigDecimal.valueOf(50));
        creditResponse.setTotalAmount(BigDecimal.valueOf(1000));

        CreditTransaction transaction = new CreditTransaction();
        transaction.setCreditId("123");

        CreditPaymentTransactionResponse response = new CreditPaymentTransactionResponse();
        response.setCreditId("123");

        when(creditService.getCreditById("123"))
            .thenReturn(Mono.just(creditResponse));

        when(customerService.getCustomerById("456"))
            .thenReturn(Mono.just(new CustomerResponse()));

        when(creditTransactionMapper.getCreditPaymentTransactionEntity(any()))
            .thenReturn(transaction);

        when(creditTransactionRepository.save(any(CreditTransaction.class)))
            .thenReturn(Mono.just(transaction));

        when(creditService.updateCreditById(anyString(), any(CreditPatchRequest.class)))
            .thenReturn(Mono.just(creditResponse));

        when(creditTransactionMapper.getCreditPaymentTransactionResponse(any()))
            .thenReturn(response);

        StepVerifier.create(creditTransactionService.createCreditPaymentTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createCreditPaymentTransaction_NoAmountPaidSuccess() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        request.setCreditId("123");
        request.setCustomerId("456");

        CreditResponse creditResponse = new CreditResponse();
        creditResponse.setMonthlyPayment(BigDecimal.valueOf(50));
        creditResponse.setTotalAmount(BigDecimal.valueOf(1000));

        CreditTransaction transaction = new CreditTransaction();
        transaction.setCreditId("123");

        CreditPaymentTransactionResponse response = new CreditPaymentTransactionResponse();
        response.setCreditId("123");

        when(creditService.getCreditById("123"))
            .thenReturn(Mono.just(creditResponse));

        when(customerService.getCustomerById("456"))
            .thenReturn(Mono.just(new CustomerResponse()));

        when(creditTransactionMapper.getCreditPaymentTransactionEntity(any()))
            .thenReturn(transaction);

        when(creditTransactionRepository.save(any(CreditTransaction.class)))
            .thenReturn(Mono.just(transaction));

        when(creditService.updateCreditById(anyString(), any(CreditPatchRequest.class)))
            .thenReturn(Mono.just(creditResponse));

        when(creditTransactionMapper.getCreditPaymentTransactionResponse(any()))
            .thenReturn(response);

        StepVerifier.create(creditTransactionService.createCreditPaymentTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createCreditPaymentTransaction_FullyPaid() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        request.setCreditId("123");
        request.setCustomerId("456");

        CreditResponse creditResponse = new CreditResponse();
        creditResponse.setAmountPaid(BigDecimal.valueOf(950));
        creditResponse.setMonthlyPayment(BigDecimal.valueOf(100));
        creditResponse.setTotalAmount(BigDecimal.valueOf(1000));

        when(creditService.getCreditById("123")).thenReturn(Mono.just(creditResponse));
        when(customerService.getCustomerById("456")).thenReturn(Mono.just(new CustomerResponse()));

        StepVerifier.create(creditTransactionService.createCreditPaymentTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void getCreditTransactionsByCreditId_Success() {
        String creditId = "123";
        CreditTransaction transaction = new CreditTransaction();
        transaction.setType(CreditTransactionType.PAYMENT);
        transaction.setCreditId(creditId);

        CreditPaymentTransactionResponse response = new CreditPaymentTransactionResponse();
        response.setCreditId(creditId);

        when(creditService.getCreditById(creditId))
            .thenReturn(Mono.just(new CreditResponse()));
        when(creditTransactionRepository.findAllByCreditId(creditId))
            .thenReturn(Flux.just(transaction));
        when(creditTransactionMapper.getCreditPaymentTransactionResponse(transaction))
            .thenReturn(response);

        StepVerifier.create(creditTransactionService.getCreditTransactionsByCreditId(creditId))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void validateAndGetCredit_NotFound() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        request.setCreditId("123");

        when(creditService.getCreditById("123")).thenReturn(Mono.error(new NotFoundException("Credit not found")));

        StepVerifier.create(creditTransactionService.createCreditPaymentTransaction(Mono.just(request)))
            .expectError()
            .verify();
    }

    @Test
    void validateCustomer_NotFound() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        request.setCreditId("123");
        request.setCustomerId("456");

        when(creditService.getCreditById("123")).thenReturn(Mono.just(new CreditResponse()));
        when(customerService.getCustomerById("456")).thenReturn(Mono.error(new NotFoundException("Customer not found")));

        StepVerifier.create(creditTransactionService.createCreditPaymentTransaction(Mono.just(request)))
            .expectError(NotFoundException.class)
            .verify();
    }
}
