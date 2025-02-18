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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountTransactionServiceImpl implements AccountTransactionService {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountTransactionMapper accountTransactionMapper;

    @Autowired
    AccountTransactionRepository accountTransactionRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public Mono<AccountTransactionResponse> createAccountTransaction(Mono<AccountTransactionRequest> request) {
        return request
                .flatMap(this::validateTransactionType)
                .flatMap(this::validateTransactionRequest)
                .flatMap(req ->
                        this.getOriginAndDestinationAccounts(req)
                                .flatMap(this::validateAccounts)
                                .flatMap(this::validateAccountMonthlyMovements)
                                .flatMap(tuple -> this.validateAccountBalance(tuple, req))
                                .flatMap(tuple -> this.processTransaction(tuple, req)))
                .map(accountTransactionMapper::getAccountTransactionResponse)
                .doOnSuccess(t -> log.info("Transaction created: {}", t.getId()))
                .doOnError(e -> log.error("Error creating transaction", e));
    }

    /**
     * Validate if the transaction type sent in request body is part of the enum one
     *
     * @param req Transaction request
     * @return Mono of TransactionRequest
     */
    private Mono<AccountTransactionRequest> validateTransactionType(AccountTransactionRequest req) {
        try {
            AccountTransactionType.valueOf(req.getType());
            return Mono.just(req);
        } catch (IllegalArgumentException ex) {
            String result = Arrays.stream(AccountTransactionType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining("|"));
            return Mono.error(new BadRequestException("Invalid transaction type. Should be one of: " + result));
        }
    }

    /**
     * Validates the transaction request based on transaction type
     * @param req The transaction request to validate
     * @return Mono containing the validated request
     * @throws BadRequestException if destination account is missing for TRANSFER type
     */
    private Mono<AccountTransactionRequest> validateTransactionRequest(AccountTransactionRequest req) {
        if (AccountTransactionType.TRANSFER.toString().equals(req.getType())) {
            if (req.getDestinationAccountNumber() == null) {
                return Mono.error(new BadRequestException("destinationAccountNumber is required for TRANSFER transactions"));
            }
        }
        return Mono.just(req);
    }

    /**
     * Retrieves both origin and destination accounts for a transaction
     * @param request The transaction request containing account numbers
     * @return Mono containing a tuple of origin and destination account responses
     */
    private Mono<Tuple2<AccountResponse, AccountResponse>> getOriginAndDestinationAccounts(AccountTransactionRequest request) {
        Mono<AccountResponse> originAccountMono = accountService.getAccountByAccountNumber(request.getOriginAccountNumber());
        Mono<AccountResponse> destionationAccountMono = accountService.getAccountByAccountNumber(request.getDestinationAccountNumber());
        return Mono.zip(originAccountMono, destionationAccountMono);
    }

    /**
     * Retrieves all transactions for a given account number
     * @param accountNumber The account number to get transactions for
     * @return Flux of transaction responses associated with the account
     */
    @Override
    public Flux<AccountTransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        return accountService.getAccountByAccountNumber(accountNumber)
                .flatMapMany(account ->
                        accountTransactionRepository.findAllByDestinationAccountNumber(accountNumber)
                                .map(accountTransactionMapper::getAccountTransactionResponse)
                );
    }

    /**
     * Validates that both accounts involved in the transaction are active
     * @param tuple Tuple containing origin and destination account responses
     * @return Mono containing validated accounts tuple
     * @throws BadRequestException if either account is not in ACTIVE status
     */
    private Mono<Tuple2<AccountResponse, AccountResponse>> validateAccounts(Tuple2<AccountResponse, AccountResponse> tuple) {
        AccountResponse originAccount = tuple.getT1();
        AccountResponse destinationAccount = tuple.getT2();
        CustomerType originAccountCustomerType = CustomerType.valueOf(originAccount.getCustomerType());
        CustomerType destinationAccountCustomerType = CustomerType.valueOf(destinationAccount.getCustomerType());
        if (!AccountStatus.ACTIVE.toString().equals(originAccount.getStatus())) {
            return Mono.error(new BadRequestException("origin ACCOUNT has " + originAccount.getStatus() + " status"));
        }
        if (!AccountStatus.ACTIVE.toString().equals(originAccount.getStatus())) {
            return Mono.error(new BadRequestException("destination ACCOUNT has " + originAccount.getStatus() + " status"));
        }
        // validate if accounts are compatible (PERSONAL accounts cannot transfer to BUSINESS accounts)
        if (originAccountCustomerType.equals(CustomerType.PERSONAL) && destinationAccountCustomerType.equals(CustomerType.BUSINESS)) {
            return Mono.error(new BadRequestException("PERSONAL accounts cannot transfer to BUSINESS accounts"));
        }
        if (originAccountCustomerType.equals(CustomerType.BUSINESS) && destinationAccountCustomerType.equals(CustomerType.PERSONAL)) {
            return Mono.error(new BadRequestException("BUSINESS accounts cannot transfer to PERSONAL accounts"));
        }
        return Mono.just(Tuples.of(originAccount, destinationAccount));
    }

    /**
     * Validates account movement restrictions based on account type
     * @param tuple Tuple containing origin and destination account responses
     * @return Mono containing validated accounts tuple
     * @throws BadRequestException if movement restrictions are violated
     */
    private Mono<Tuple2<AccountResponse, AccountResponse>> validateAccountMonthlyMovements(Tuple2<AccountResponse, AccountResponse> tuple) {
        AccountResponse originAccount = tuple.getT1();
        AccountResponse destinationAccount = tuple.getT2();
        AccountType originAccountType = AccountType.valueOf(originAccount.getAccountType());
        Integer originAccountMonthlyMovements = Optional.ofNullable(originAccount.getMonthlyMovements()).orElse(0);
        Integer originAccountMaxMonthlyMovements = Optional.ofNullable(originAccount.getMaxMonthlyMovements()).orElse(0);

        if (AccountType.SAVINGS.equals(originAccountType) || AccountType.FIXED_TERM.equals(originAccountType)) {
            if (originAccountMonthlyMovements >= originAccountMaxMonthlyMovements) {
                return Mono.error(new BadRequestException("Max monthly movements reached for origin ACCOUNT"));
            }
        }

        if (AccountType.FIXED_TERM.equals(originAccountType)) {
            Integer availableDay = Optional.ofNullable(originAccount.getAvailableDayForMovements()).orElse(1);
            LocalDate today = LocalDate.now();
            if (today.getDayOfMonth() != availableDay) {
                return Mono.error(new BadRequestException("FIXED_TERM account can only make transactions on " + availableDay + "th of each month"));
            }
        }

        return Mono.just(Tuples.of(originAccount, destinationAccount));
    }

    /**
     * Validates if account has sufficient balance for withdrawal or transfer transactions
     * @param tuple Tuple containing origin and destination account responses
     * @param req The transaction request containing type and amount
     * @return Mono containing validated accounts tuple
     * @throws BadRequestException if balance is insufficient
     */
    private Mono<Tuple2<AccountResponse, AccountResponse>> validateAccountBalance(Tuple2<AccountResponse, AccountResponse> tuple, AccountTransactionRequest req) {
        AccountResponse originAccount = tuple.getT1();
        AccountTransactionType accountTransactionType = AccountTransactionType.valueOf(req.getType());

        // if transaction request type is WITHDRAWAL or TRANSFER validate the current account balance of the origin account
        if (accountTransactionType.equals(AccountTransactionType.WITHDRAWAL) || accountTransactionType.equals(AccountTransactionType.TRANSFER)) {
            BigDecimal balance = Optional.ofNullable(originAccount.getBalance()).orElse(BigDecimal.ZERO);
            if (balance.compareTo(req.getAmount()) < 0) {
                return Mono.error(new BadRequestException("Insufficient balance to complete the transaction"));
            }
        }

        return Mono.just(tuple);
    }

    /**
     * Processes a transaction between accounts, handling both single account transactions and transfers
     *
     * @param accounts Tuple containing origin and destination account responses
     * @param req      The transaction request details
     * @return Mono containing the processed transaction
     */
    private Mono<Transaction> processTransaction(Tuple2<AccountResponse, AccountResponse> accounts, AccountTransactionRequest req) {
        return processAccountTransaction(accounts.getT1(), req, true)
                .flatMap(transaction -> AccountTransactionType.TRANSFER.toString().equals(req.getType())
                        ? processAccountTransaction(accounts.getT2(), req, false)
                        : Mono.just(transaction));
    }

    /**
     * Processes a single account transaction
     *
     * @param account  The account to process the transaction for
     * @param req      The transaction request details
     * @param isOrigin Whether this is the origin account in a transfer
     * @return Mono containing the processed transaction
     */
    private Mono<Transaction> processAccountTransaction(AccountResponse account, AccountTransactionRequest req, boolean isOrigin) {
        Transaction transaction = accountTransactionMapper.getAccountTransactionEntity(req);
        return saveTransactionAndUpdateAccount(transaction, account, isOrigin);
    }

    /**
     * Saves the transaction and updates the associated account
     *
     * @param transaction The transaction to save
     * @param account     The account to update
     * @param isOrigin    Whether this is the origin account in a transfer
     * @return Mono containing the saved transaction
     */
    private Mono<Transaction> saveTransactionAndUpdateAccount(Transaction transaction, AccountResponse account, boolean isOrigin) {
        return transactionRepository.save(transaction)
                .flatMap(savedTransaction -> updateAccountBalance(account, savedTransaction, isOrigin));
    }

    /**
     * Updates the account balance and returns the transaction
     *
     * @param account     The account to update
     * @param transaction The transaction details
     * @param isOrigin    Whether this is the origin account in a transfer
     * @return Mono containing the transaction
     */
    private Mono<Transaction> updateAccountBalance(AccountResponse account, Transaction transaction, boolean isOrigin) {
        return accountService.updateAccount(
                        account.getId(),
                        accountTransactionMapper.getAccountPatchRequest(transaction, account, isOrigin))
                .thenReturn(transaction);
    }
}
