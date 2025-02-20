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
                        getOriginAndDestinationAccounts(req)
                                .flatMap(tuple -> validateAccounts(tuple, req))
                                .flatMap(this::validateAccountMonthlyMovements)
                                .flatMap(tuple -> validateAccountBalance(tuple, req))
                                .flatMap(tuple -> processTransaction(tuple, req)))
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
     *
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
     *
     * @param request The transaction request containing account numbers
     * @return Mono containing a tuple of origin and destination account responses
     */
    private Mono<Tuple2<AccountResponse, Optional<AccountResponse>>> getOriginAndDestinationAccounts(AccountTransactionRequest request) {
        Mono<AccountResponse> originAccountMono = accountService.getAccountByAccountNumber(request.getOriginAccountNumber());
        Mono<Optional<AccountResponse>> destionationAccountMono = Mono.just(Optional.empty());
        if (request.getDestinationAccountNumber() != null) {
            destionationAccountMono = accountService.getAccountByAccountNumber(request.getDestinationAccountNumber())
                    .map(Optional::ofNullable);
        }
        return Mono.zip(originAccountMono, destionationAccountMono);
    }

    /**
     * Retrieves all transactions for a given account number
     *
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
     *
     * @param tuple Tuple containing origin and optional destination account responses
     * @return Mono containing validated accounts tuple
     * @throws BadRequestException if either account is not in ACTIVE status
     */
    private Mono<Tuple2<AccountResponse, Optional<AccountResponse>>> validateAccounts(Tuple2<AccountResponse, Optional<AccountResponse>> tuple, AccountTransactionRequest req) {
        AccountResponse originAccount = tuple.getT1();
        Optional<AccountResponse> destinationAccountOptional = tuple.getT2();
        CustomerType originAccountCustomerType = CustomerType.valueOf(originAccount.getCustomerType());
        AccountType originAccountType = AccountType.valueOf(originAccount.getAccountType());
        AccountTransactionType accountTransactionType = AccountTransactionType.valueOf(req.getType());
        if (!AccountStatus.ACTIVE.toString().equals(originAccount.getStatus())) {
            return Mono.error(new BadRequestException("origin ACCOUNT has " + originAccount.getStatus() + " status"));
        }
        if (!AccountStatus.ACTIVE.toString().equals(originAccount.getStatus())) {
            return Mono.error(new BadRequestException("destination ACCOUNT has " + originAccount.getStatus() + " status"));
        }
        if (accountTransactionType == AccountTransactionType.TRANSFER) {
            // transfer transactions cannot be made from fixed term account
            if (originAccountType == AccountType.FIXED_TERM) {
                return Mono.error(new BadRequestException("TRANSFER transactions cannot be made from FIXED_TERM account"));
            }

            if (destinationAccountOptional.isPresent()) {
                AccountResponse destinationAccount = destinationAccountOptional.get();
                AccountType destinationAccountType = AccountType.valueOf(destinationAccount.getAccountType());
                CustomerType destinationAccountCustomerType = CustomerType.valueOf(destinationAccount.getCustomerType());
                // validate if accounts are compatible (PERSONAL accounts cannot transfer to BUSINESS accounts)
                if (originAccountCustomerType.equals(CustomerType.PERSONAL) && destinationAccountCustomerType.equals(CustomerType.BUSINESS)) {
                    return Mono.error(new BadRequestException("PERSONAL accounts cannot transfer to BUSINESS accounts"));
                }
                if (originAccountCustomerType.equals(CustomerType.BUSINESS) && destinationAccountCustomerType.equals(CustomerType.PERSONAL)) {
                    return Mono.error(new BadRequestException("BUSINESS accounts cannot transfer to PERSONAL accounts"));
                }
                if (destinationAccountType == AccountType.FIXED_TERM) {
                    return Mono.error(new BadRequestException("TRANSFER transactions cannot be carried out to FIXED_TERM accounts"));
                }
            }
        }

        return Mono.just(Tuples.of(originAccount, destinationAccountOptional));
    }

    /**
     * Validates account movement restrictions based on account type
     *
     * @param tuple Tuple containing origin and optional destination account responses
     * @return Mono containing validated accounts tuple
     * @throws BadRequestException if movement restrictions are violated
     */
    private Mono<Tuple2<AccountResponse, Optional<AccountResponse>>> validateAccountMonthlyMovements(Tuple2<AccountResponse, Optional<AccountResponse>> tuple) {
        AccountResponse originAccount = tuple.getT1();
        Optional<AccountResponse> destinationAccountOptional = tuple.getT2();
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

        return Mono.just(Tuples.of(originAccount, destinationAccountOptional));
    }

    /**
     * Validates if account has sufficient balance for withdrawal or transfer transactions
     *
     * @param tuple Tuple containing origin and optional of destination account responses
     * @param req   The transaction request containing type and amount
     * @return Mono containing validated accounts tuple
     * @throws BadRequestException if balance is insufficient
     */
    private Mono<Tuple2<AccountResponse, Optional<AccountResponse>>> validateAccountBalance(Tuple2<AccountResponse, Optional<AccountResponse>> tuple, AccountTransactionRequest req) {
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
     * Processes a transaction by saving it and updating account balances.
     * For transfers, updates both origin and destination account balances.
     *
     * @param accounts Tuple containing origin account and optional destination account
     * @param req      Transaction request details
     * @return Mono<Transaction> with processed transaction
     */
    private Mono<AccountTransaction> processTransaction(Tuple2<AccountResponse, Optional<AccountResponse>> accounts, AccountTransactionRequest req) {
        AccountResponse originAccount = accounts.getT1();
        AccountTransaction transaction = accountTransactionMapper.getAccountTransactionEntity(req, originAccount);

        return accountTransactionRepository.save(transaction)
                .flatMap(savedTransaction -> updateAccountBalance(originAccount, savedTransaction, true))
                .flatMap(savedTransaction -> {
                    if (AccountTransactionType.TRANSFER.toString().equals(req.getType())) {
                        Optional<AccountResponse> destinationAccountOptional = accounts.getT2();
                        return destinationAccountOptional.map(destinationAccount ->
                                        updateAccountBalance(destinationAccount, savedTransaction, false)
                                )
                                .orElseGet(() -> Mono.just(transaction));
                    } else {
                        return Mono.just(transaction);
                    }
                });
    }

    /**
     * Updates the account balance and returns the transaction
     *
     * @param account     The account to update
     * @param transaction The transaction details
     * @param isOrigin    Whether this is the origin account in a transfer
     * @return Mono containing the transaction
     */
    private Mono<AccountTransaction> updateAccountBalance(AccountResponse account, AccountTransaction transaction, boolean isOrigin) {
        return accountService.updateAccount(
                        account.getId(),
                        accountTransactionMapper.getAccountPatchRequest(transaction, account, isOrigin))
                .thenReturn(transaction);
    }
}
