package com.project1.ms_transaction_service.business.service.impl;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.business.adapter.DebitCardService;
import com.project1.ms_transaction_service.business.mapper.DebitCardTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.NotFoundException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.AccountStatus;
import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import com.project1.ms_transaction_service.model.entity.DebitCardTransactionType;
import com.project1.ms_transaction_service.repository.DebitCardTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
class DebitCardTransactionServiceImplTest {

    @Autowired
    private DebitCardTransactionServiceImpl debitCardTransactionService;

    @MockBean
    private DebitCardTransactionRepository debitCardTransactionRepository;

    @MockBean
    private DebitCardService debitCardService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private DebitCardTransactionMapper debitCardTransactionMapper;

    @Test
    void createDebitCardTransaction_Success() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(100));

        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();
        DebitCardAssociation association = new DebitCardAssociation();
        association.setAccountId("456");
        association.setPosition(1);
        associations.add(association);
        debitCard.setAssociations(associations);

        AccountResponse account = new AccountResponse();
        account.setId("456");
        account.setBalance(BigDecimal.valueOf(200));
        account.setCustomerId("789");

        DebitCardTransaction transaction = new DebitCardTransaction();
        transaction.setAccountId("456");

        DebitCardTransactionResponse response = new DebitCardTransactionResponse();

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("456"))
            .thenReturn(Mono.just(account));
        when(debitCardTransactionMapper.getDebitCardTransactionEntity(request))
            .thenReturn(transaction);
        when(debitCardTransactionRepository.save(any(DebitCardTransaction.class)))
            .thenReturn(Mono.just(transaction));
        when(accountService.updateAccount(anyString(), any()))
            .thenReturn(Mono.just(account));
        when(debitCardTransactionMapper.getDebitCardTransactionResponse(any()))
            .thenReturn(response);

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createDebitCardTransaction_NoAssociatedAccounts() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");

        DebitCardResponse debitCard = new DebitCardResponse();
        debitCard.setAssociations(new ArrayList<>());

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createDebitCardTransaction_ShouldInsufficientFundsFail() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(300));

        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();
        DebitCardAssociation association = new DebitCardAssociation();
        association.setAccountId("456");
        association.setPosition(1);
        associations.add(association);
        debitCard.setAssociations(associations);

        AccountResponse account = new AccountResponse();
        account.setId("456");
        account.setBalance(BigDecimal.valueOf(200));

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("456"))
            .thenReturn(Mono.just(account));

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createDebitCardTransaction_ShouldInactiveAccountFail() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(300));

        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();
        DebitCardAssociation association = new DebitCardAssociation();
        association.setAccountId("456");
        association.setPosition(1);
        associations.add(association);
        debitCard.setAssociations(associations);

        AccountResponse account = new AccountResponse();
        account.setId("456");
        account.setStatus(AccountStatus.INACTIVE.toString());
        account.setBalance(BigDecimal.valueOf(200));

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("456"))
            .thenReturn(Mono.just(account));

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createDebitCardTransaction_DebitCardNotFound() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.error(new NotFoundException("Debit card not found")));

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectError(NotFoundException.class)
            .verify();
    }

    @Test
    void createDebitCardTransaction_AccountNotFound() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");

        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();
        DebitCardAssociation association = new DebitCardAssociation();
        association.setAccountId("456");
        association.setPosition(1);
        associations.add(association);
        debitCard.setAssociations(associations);

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("456"))
            .thenReturn(Mono.error(new NotFoundException("Account not found")));

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectError(NotFoundException.class)
            .verify();
    }

    @Test
    void createDebitCardTransaction_NoAccountBalanceSuccess() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(100));

        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();
        DebitCardAssociation association = new DebitCardAssociation();
        association.setAccountId("456");
        association.setPosition(1);
        associations.add(association);
        debitCard.setAssociations(associations);

        AccountResponse account = new AccountResponse();
        account.setId("456");
        account.setBalance(BigDecimal.valueOf(200));
        account.setCustomerId("789");

        DebitCardTransaction transaction = new DebitCardTransaction();
        transaction.setAccountId("456");

        DebitCardTransactionResponse response = new DebitCardTransactionResponse();

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("456"))
            .thenReturn(Mono.just(account));
        when(debitCardTransactionMapper.getDebitCardTransactionEntity(request))
            .thenReturn(transaction);
        when(debitCardTransactionRepository.save(any(DebitCardTransaction.class)))
            .thenReturn(Mono.just(transaction));
        when(accountService.updateAccount(anyString(), any()))
            .thenReturn(Mono.just(account));
        when(debitCardTransactionMapper.getDebitCardTransactionResponse(any()))
            .thenReturn(response);

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createDebitCardTransaction_RecursiveAccountSearch() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transaction");
        request.setType(DebitCardTransactionType.PURCHASE.toString());

        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();

        DebitCardAssociation association1 = new DebitCardAssociation();
        association1.setAccountId("456");
        association1.setPosition(1);

        DebitCardAssociation association2 = new DebitCardAssociation();
        association2.setAccountId("457");
        association2.setPosition(2);

        DebitCardAssociation association3 = new DebitCardAssociation();
        association3.setAccountId("458");
        association3.setPosition(3);

        associations.add(association1);
        associations.add(association2);
        associations.add(association3);
        debitCard.setAssociations(associations);

        // Setup accounts with different balances
        AccountResponse account1 = new AccountResponse();
        account1.setId("456");
        account1.setBalance(BigDecimal.valueOf(50)); // Insufficient funds
        account1.setCustomerId("789");

        AccountResponse account2 = new AccountResponse();
        account2.setId("457");
        account2.setBalance(BigDecimal.valueOf(75)); // Insufficient funds
        account2.setCustomerId("789");

        AccountResponse account3 = new AccountResponse();
        account3.setId("458");
        account3.setBalance(BigDecimal.valueOf(150)); // Sufficient funds
        account3.setCustomerId("789");

        DebitCardTransaction transaction = new DebitCardTransaction();
        transaction.setAccountId("458"); // Should end up using this account
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCustomerId("789");
        transaction.setDebitCardId("123");
        transaction.setDescription("Test transaction");
        transaction.setType(DebitCardTransactionType.PURCHASE);

        DebitCardTransactionResponse response = new DebitCardTransactionResponse();
        response.setDebitCardId("123");
        response.setCustomerId("789");
        response.setAmount(BigDecimal.valueOf(100));
        response.setDescription("Test transaction");
        response.setType(DebitCardTransactionType.PURCHASE.toString());

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));

        when(accountService.getAccountById("456"))
            .thenReturn(Mono.just(account1));
        when(accountService.getAccountById("457"))
            .thenReturn(Mono.just(account2));
        when(accountService.getAccountById("458"))
            .thenReturn(Mono.just(account3));

        when(debitCardTransactionMapper.getDebitCardTransactionEntity(request))
            .thenReturn(transaction);
        when(debitCardTransactionRepository.save(any(DebitCardTransaction.class)))
            .thenReturn(Mono.just(transaction));
        when(accountService.updateAccount(anyString(), any()))
            .thenReturn(Mono.just(account3));
        when(debitCardTransactionMapper.getDebitCardTransactionResponse(any()))
            .thenReturn(response);

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();

        // Verify that all accounts were checked in order
        verify(accountService).getAccountById("456");
        verify(accountService).getAccountById("457");
        verify(accountService).getAccountById("458");

        // Verify that update was performed only on the account with sufficient funds
        verify(accountService).updateAccount(eq("458"), any());
    }

    @Test
    void createDebitCardTransaction_RecursiveAccountSearch_InactiveStatus() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transaction");
        request.setType(DebitCardTransactionType.PURCHASE.toString());

        // Setup debit card with multiple account associations
        DebitCardResponse debitCard = new DebitCardResponse();
        List<DebitCardAssociation> associations = new ArrayList<>();

        DebitCardAssociation association1 = new DebitCardAssociation();
        association1.setAccountId("456");
        association1.setPosition(1);

        DebitCardAssociation association3 = new DebitCardAssociation();
        association3.setAccountId("458");
        association3.setPosition(3);

        associations.add(association1);
        associations.add(association3);
        debitCard.setAssociations(associations);

        // Setup accounts with different statuses
        AccountResponse account1 = new AccountResponse();
        account1.setId("456");
        account1.setBalance(BigDecimal.valueOf(200));
        account1.setCustomerId("789");
        account1.setStatus(AccountStatus.INACTIVE.toString()); // First account INACTIVE

        AccountResponse account3 = new AccountResponse();
        account3.setId("458");
        account3.setBalance(BigDecimal.valueOf(150));
        account3.setCustomerId("789");
        account3.setStatus(AccountStatus.ACTIVE.toString()); // Second account ACTIVE

        DebitCardTransaction transaction = new DebitCardTransaction();
        transaction.setAccountId("458");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCustomerId("789");
        transaction.setDebitCardId("123");
        transaction.setDescription("Test transaction");
        transaction.setType(DebitCardTransactionType.PURCHASE);

        DebitCardTransactionResponse response = new DebitCardTransactionResponse();
        response.setDebitCardId("123");
        response.setCustomerId("789");
        response.setAmount(BigDecimal.valueOf(100));
        response.setDescription("Test transaction");
        response.setType(DebitCardTransactionType.PURCHASE.toString());

        when(debitCardService.getDebitCardById("123"))
            .thenReturn(Mono.just(debitCard));

        when(accountService.getAccountById("456"))
            .thenReturn(Mono.just(account1));
        when(accountService.getAccountById("458"))
            .thenReturn(Mono.just(account3));

        when(debitCardTransactionMapper.getDebitCardTransactionEntity(request))
            .thenReturn(transaction);
        when(debitCardTransactionRepository.save(any(DebitCardTransaction.class)))
            .thenReturn(Mono.just(transaction));
        when(accountService.updateAccount(anyString(), any()))
            .thenReturn(Mono.just(account3));
        when(debitCardTransactionMapper.getDebitCardTransactionResponse(any()))
            .thenReturn(response);

        StepVerifier.create(debitCardTransactionService.createDebitCardTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();

        // Verify that all accounts were checked in order
        verify(accountService).getAccountById("456");
        verify(accountService).getAccountById("458");

        // Verify that update was performed only on the active account
        verify(accountService).updateAccount(eq("458"), any());
    }
}
