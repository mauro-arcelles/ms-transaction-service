package com.project1.ms_transaction_service.business.service;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.business.mapper.AccountTransactionMapper;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.entity.*;
import com.project1.ms_transaction_service.repository.AccountTransactionRepository;
import com.project1.ms_transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.*;

import static org.mockito.Mockito.*;

@SpringBootTest
class AccountTransactionServiceImplTest {

    private static final LocalDate LOCAL_DATE = LocalDate.of(2025, 1, 13);

    @Autowired
    private AccountTransactionServiceImpl accountTransactionService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountTransactionMapper accountTransactionMapper;

    @MockBean
    private AccountTransactionRepository accountTransactionRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private Clock clock;

    private Clock fixedClock;

    @BeforeEach
    public void setup() {
        fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    void createAccountTransaction_ShouldDepositSuccess() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.DEPOSIT.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setStatus(AccountStatus.ACTIVE.toString());
        accountResponse.setBalance(BigDecimal.ZERO);
        accountResponse.setAccountType(AccountType.SAVINGS.toString());
        accountResponse.setCustomerType(CustomerType.PERSONAL.toString());
        accountResponse.setMaxMonthlyMovements(5);
        accountResponse.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(accountResponse));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(accountResponse));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createAccountTransaction_ShouldTransferSuccess() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("10"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("123");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createAccountTransaction_ShouldInsufficientFundsTransferFail() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("123");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldInactiveOriginAccountForTransferFail() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.INACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("234");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldInactiveDestinationAccountForTransferFail() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("234");
        destinationAccount.setStatus(AccountStatus.INACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldTransferTransactionsBetweenFixedTermAccountsFail() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.FIXED_TERM.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("234");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldTransferTransactionsBetweenFixedTermAccountsFail2() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("234");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.FIXED_TERM.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldTransferTransactionsBetweenPersonalAndBusinessAccountsFail() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("234");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.BUSINESS.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldTransferTransactionsBetweenPersonalAndBusinessAccountsFail2() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.TRANSFER.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.SAVINGS.toString());
        originAccount.setCustomerType(CustomerType.BUSINESS.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("9"));

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("234");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void getTransactionsByAccountNumber_Success() {
        String accountNumber = "123";
        AccountResponse account = new AccountResponse();
        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber(accountNumber))
            .thenReturn(Mono.just(account));
        when(accountTransactionRepository.findAllByOriginAccountNumber(accountNumber))
            .thenReturn(Flux.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(transaction))
            .thenReturn(response);

        StepVerifier.create(accountTransactionService.getTransactionsByAccountNumber(accountNumber))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void validateTransferTransaction_MissingDestinationAccount() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType("TRANSFER");
        request.setOriginAccountNumber("123");

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void validateAccountBalance_InsufficientBalance() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType("WITHDRAWAL");
        request.setAmount(BigDecimal.valueOf(100));
        request.setOriginAccountNumber("123");

        AccountResponse account = new AccountResponse();
        account.setStatus("ACTIVE");
        account.setBalance(BigDecimal.valueOf(50));
        account.setAccountType("SAVINGS");
        account.setCustomerType("PERSONAL");

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(account));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createAccountTransaction_ShouldDepositForFixedTermAccountSuccess() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.DEPOSIT.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");


        fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.FIXED_TERM.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("10"));
        originAccount.setAvailableDayForMovements(13);

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("123");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createAccountTransaction_ShouldDepositForFixedTermAccountFail() {
        AccountTransactionRequest request = new AccountTransactionRequest();
        request.setType(AccountTransactionType.DEPOSIT.toString());
        request.setAmount(BigDecimal.TEN);
        request.setOriginAccountNumber("123");
        request.setDestinationAccountNumber("234");

        AccountResponse originAccount = new AccountResponse();
        originAccount.setId("123");
        originAccount.setStatus(AccountStatus.ACTIVE.toString());
        originAccount.setBalance(BigDecimal.ZERO);
        originAccount.setAccountType(AccountType.FIXED_TERM.toString());
        originAccount.setCustomerType(CustomerType.PERSONAL.toString());
        originAccount.setMaxMonthlyMovements(5);
        originAccount.setMonthlyMovements(1);
        originAccount.balance(new BigDecimal("10"));
        originAccount.setAvailableDayForMovements(6);

        AccountResponse destinationAccount = new AccountResponse();
        destinationAccount.setId("123");
        destinationAccount.setStatus(AccountStatus.ACTIVE.toString());
        destinationAccount.setBalance(BigDecimal.ZERO);
        destinationAccount.setAccountType(AccountType.SAVINGS.toString());
        destinationAccount.setCustomerType(CustomerType.PERSONAL.toString());
        destinationAccount.setMaxMonthlyMovements(5);
        destinationAccount.setMonthlyMovements(1);

        AccountTransaction transaction = new AccountTransaction();
        AccountTransactionResponse response = new AccountTransactionResponse();

        when(accountService.getAccountByAccountNumber("123"))
            .thenReturn(Mono.just(originAccount));
        when(accountService.getAccountByAccountNumber("234"))
            .thenReturn(Mono.just(destinationAccount));
        when(accountTransactionMapper.getAccountTransactionEntity(any(), any()))
            .thenReturn(transaction);
        when(accountTransactionRepository.save(any()))
            .thenReturn(Mono.just(transaction));
        when(accountTransactionMapper.getAccountTransactionResponse(any()))
            .thenReturn(response);
        when(accountService.updateAccount(any(), any()))
            .thenReturn(Mono.just(originAccount));

        StepVerifier.create(accountTransactionService.createAccountTransaction(Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }
}
