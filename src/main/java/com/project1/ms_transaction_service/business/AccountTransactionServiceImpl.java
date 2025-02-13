package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.entity.AccountStatus;
import com.project1.ms_transaction_service.model.entity.AccountTransactionType;
import com.project1.ms_transaction_service.model.entity.AccountType;
import com.project1.ms_transaction_service.model.entity.Transaction;
import com.project1.ms_transaction_service.repository.AccountTransactionRepository;
import com.project1.ms_transaction_service.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

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
                .flatMap(this::validateAccount)
                .flatMap(this::processTransaction)
                .map(accountTransactionMapper::getAccountTransactionResponse)
                .doOnSuccess(t -> log.info("Transaction created: {}", t.getId()))
                .doOnError(e -> log.error("Error creating transaction", e));
    }

    @Override
    public Flux<AccountTransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        return accountService.getAccountByAccountNumber(accountNumber)
                .flatMapMany(account ->
                        accountTransactionRepository.findAllByDestinationAccountNumber(accountNumber)
                                .map(accountTransactionMapper::getAccountTransactionResponse)
                );
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
            return Mono.error(new BadRequestException("Invalid transaction type. Should be one of: DEPOSIT|WITHDRAWAL"));
        }
    }

    /**
     * Validate if the account exists and if it has available monthly movements
     *
     * @param req Transaction request
     * @return Mono of Transaction
     */
    private Mono<AccountTransactionRequest> validateAccount(AccountTransactionRequest req) {
        return accountService.getAccountByAccountNumber(req.getDestinationAccountNumber())
                .flatMap(account -> {
                    if (!AccountStatus.ACTIVE.toString().equals(account.getStatus())) {
                        return Mono.error(new BadRequestException("ACCOUNT has " + account.getStatus() + " status"));
                    }

                    AccountType accountType = AccountType.valueOf(account.getAccountType());
                    Integer monthlyMovements = Optional.ofNullable(account.getMonthlyMovements()).orElse(0);
                    Integer maxMonthlyMovements = Optional.ofNullable(account.getMaxMonthlyMovements()).orElse(0);

                    if (accountType.equals(AccountType.SAVINGS)) {
                        if (monthlyMovements >= maxMonthlyMovements) {
                            return Mono.error(new BadRequestException("Max monthly movements reached for account"));
                        }
                    }

                    if (accountType.equals(AccountType.FIXED_TERM)) {
                        if (monthlyMovements >= maxMonthlyMovements) {
                            return Mono.error(new BadRequestException("Max monthly movements reached for account"));
                        }

                        Integer availableDay = Optional.ofNullable(account.getAvailableDayForMovements()).orElse(1);
                        LocalDate today = LocalDate.now();
                        if (today.getDayOfMonth() != availableDay) {
                            return Mono.error(new BadRequestException("Fixed-term accounts can only make transactions on " + availableDay + "th of each month"));
                        }
                    }

                    if (AccountTransactionType.WITHDRAWAL.toString().equals(req.getType())) {
                        BigDecimal balance = Optional.ofNullable(account.getBalance()).orElse(BigDecimal.ZERO);
                        if (balance.compareTo(req.getAmount()) < 0) {
                            return Mono.error(new BadRequestException("Insufficient funds"));
                        }
                    }

                    return Mono.just(req);
                });
    }

    /**
     * Process transaction by finding account and creating transaction entity
     *
     * @param req Transaction request
     * @return Mono of Transaction
     */
    private Mono<Transaction> processTransaction(AccountTransactionRequest req) {
        return accountService.getAccountByAccountNumber(req.getDestinationAccountNumber())
                .map(account -> Tuples.of(accountTransactionMapper.getAccountTransactionEntity(req), account))
                .flatMap(tuple -> this.saveTransactionAndUpdateAccount(tuple.getT1(), tuple.getT2()));
    }

    /**
     * Save transaction and update account balance
     *
     * @param transaction     Transaction to save
     * @param accountResponse Account to update
     * @return Mono of Transaction
     */
    private Mono<Transaction> saveTransactionAndUpdateAccount(Transaction transaction, AccountResponse accountResponse) {
        return transactionRepository.save(transaction)
                .flatMap(t ->
                        accountService.updateAccount(accountResponse.getId(), accountTransactionMapper.getAccountPatchRequest(t, accountResponse))
                                .thenReturn(t)
                );
    }
}
