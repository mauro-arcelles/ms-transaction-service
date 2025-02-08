package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.TransactionRequest;
import com.project1.ms_transaction_service.model.TransactionResponse;
import com.project1.ms_transaction_service.model.entity.AccountType;
import com.project1.ms_transaction_service.model.entity.Transaction;
import com.project1.ms_transaction_service.model.entity.TransactionType;
import com.project1.ms_transaction_service.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Override
    public Mono<TransactionResponse> createTransaction(Mono<TransactionRequest> request) {
        return request
                .flatMap(this::validateTransactionType)
                .flatMap(this::validateAccount)
                .flatMap(this::processTransaction)
                .map(transactionMapper::getTransactionResponse)
                .doOnSuccess(t -> log.info("Transaction created: {}", t.getId()))
                .doOnError(e -> log.error("Error creating transaction", e));
    }

    @Override
    public Flux<TransactionResponse> getTransaccionsByAccountNumber(String accountNumber) {
        return transactionRepository.findAllByDestinationAccountNumber(accountNumber)
                .map(transactionMapper::getTransactionResponse);
    }

    /**
     * Validate if the account exists and if it has available monthly movements
     *
     * @param req Transaction request
     * @return Mono of Transaction
     */
    private Mono<TransactionRequest> validateAccount(TransactionRequest req) {
        return accountService.findAccountByAccountNumber(req.getDestinationAccountNumber())
                .flatMap(account -> {
                    AccountType accountType = AccountType.valueOf(account.getAccountType());
                    // Validate if the account has reached the max monthly movements if its SAVINGS account
                    if (account.getMonthlyMovements() >= account.getMaxMonthlyMovements()
                            && accountType.equals(AccountType.SAVINGS)) {
                        return Mono.error(new BadRequestException("Max monthly movements reached for account"));
                    }
                    // Validate if the account is FIXED_TERM, so it just can do movements once a month
                    if (accountType.equals(AccountType.FIXED_TERM)) {
                        if (account.getMonthlyMovements() >= account.getMaxMonthlyMovements()) {
                            return Mono.error(new BadRequestException("Max monthly movements reached for account"));
                        }
                        LocalDate today = LocalDate.now();
                        if (today.getDayOfMonth() != account.getAvailableDayForMovements()) {
                            return Mono.error(new BadRequestException("Fixed-term accounts can only make transactions on "+ account.getAvailableDayForMovements() + "h of each month"));
                        }
                    }
                    // Validate if the account has sufficient funds if transaction type is withdraw
                    if (TransactionType.valueOf(req.getType()) == TransactionType.WITHDRAWAL
                            && account.getBalance().compareTo(req.getAmount()) < 0) {
                        return Mono.error(new BadRequestException("Insufficient funds"));
                    }
                    return Mono.just(req);
                });
    }

    /**
     * Validate if the transaction type sent in request body is part of the enum one
     *
     * @param req Transaction request
     * @return Mono of TransactionRequest
     */
    private Mono<TransactionRequest> validateTransactionType(TransactionRequest req) {
        try {
            TransactionType.valueOf(req.getType());
            return Mono.just(req);
        } catch (IllegalArgumentException ex) {
            return Mono.error(new BadRequestException("Invalid transaction type. Should be one of: DEPOSIT|WITHDRAWAL"));
        }
    }

    /**
     * Process transaction by finding account and creating transaction entity
     *
     * @param req Transaction request
     * @return Mono of Transaction
     */
    private Mono<Transaction> processTransaction(TransactionRequest req) {
        return accountService.findAccountByAccountNumber(req.getDestinationAccountNumber())
                .map(account -> Tuples.of(transactionMapper.getTransactionEntity(req), account))
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
                        accountService.updateAccount(accountResponse.getId(), this.createAccountPatchRequest(t, accountResponse))
                                .thenReturn(t)
                );
    }

    /**
     * Create the object request to be sent to update the account
     *
     * @param transaction     Transaction to save
     * @param accountResponse Account to update
     * @return Request Object mapped of update
     */
    private AccountPatchRequest createAccountPatchRequest(Transaction transaction, AccountResponse accountResponse) {
        AccountPatchRequest accountPatchRequest = new AccountPatchRequest();
        BigDecimal newBalance = accountResponse.getBalance();
        TransactionType transactionType = TransactionType.valueOf(transaction.getType().toString());
        if (transactionType.equals(TransactionType.DEPOSIT)) {
            newBalance = newBalance.add(transaction.getAmount());
        } else {
            newBalance = newBalance.subtract(transaction.getAmount());
        }
        Integer newMovements = accountResponse.getMonthlyMovements() + 1;
        accountPatchRequest.setBalance(newBalance);
        accountPatchRequest.setMonthlyMovements(newMovements);
        return accountPatchRequest;
    }


}
