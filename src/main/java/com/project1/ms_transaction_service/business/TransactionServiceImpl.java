package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.business.adapter.AccountService;
import com.project1.ms_transaction_service.business.adapter.CreditCardService;
import com.project1.ms_transaction_service.exception.BadRequestException;
import com.project1.ms_transaction_service.exception.CreditCardCustomerMissmatchException;
import com.project1.ms_transaction_service.model.*;
import com.project1.ms_transaction_service.model.entity.*;
import com.project1.ms_transaction_service.repository.AccountTransactionRepository;
import com.project1.ms_transaction_service.repository.CreditCardTransactionRepository;
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
import java.util.Optional;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private CreditCardTransactionRepository creditCardTransactionRepository;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private AccountService accountService;

    @Override
    public Mono<AccountTransactionResponse> createAccountTransaction(Mono<AccountTransactionRequest> request) {
        return request
                .flatMap(this::validateTransactionType)
                .flatMap(this::validateAccount)
                .flatMap(this::processTransaction)
                .map(transactionMapper::getAccountTransactionResponse)
                .doOnSuccess(t -> log.info("Transaction created: {}", t.getId()))
                .doOnError(e -> log.error("Error creating transaction", e));
    }

    @Override
    public Flux<AccountTransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        return accountService.findAccountByAccountNumber(accountNumber)
                .flatMapMany(account ->
                        accountTransactionRepository.findAllByDestinationAccountNumber(accountNumber)
                                .map(transactionMapper::getAccountTransactionResponse)
                );
    }

    @Override
    public Mono<CreditCardTransactionResponse> createCreditCardTransaction(Mono<CreditCardTransactionRequest> request) {
        return request
                .flatMap(this::validateAndGetCreditCard)
                .flatMap(this::validateCreditCardLimit)
                .flatMap(this::processTransaction)
                .map(transactionMapper::getCreditCardTransactionResponse);
    }

    /**
     * Validates that the credit card exists and belongs to the customer
     * @param request The transaction request containing card and customer details
     * @return Tuple of request and card response if valid
     * @throws CreditCardCustomerMissmatchException if card doesn't belong to customer
     */
    private Mono<Tuple2<CreditCardTransactionRequest, CreditCardResponse>> validateAndGetCreditCard(CreditCardTransactionRequest request) {
        return creditCardService.getCreditCardByCardNumber(request.getCreditCard())
                .filter(card -> card.getCustomerId().equals(request.getCustomerId()))
                .switchIfEmpty(Mono.error(new CreditCardCustomerMissmatchException()))
                .map(card -> Tuples.of(request, card));
    }

    /**
     * Validates if the transaction amount is within the available credit limit
     * @param tuple Contains the transaction request and card details
     * @return The input tuple if validation passes
     * @throws BadRequestException if insufficient funds available
     */
    private Mono<Tuple2<CreditCardTransactionRequest, CreditCardResponse>> validateCreditCardLimit(Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        if (card.getUsedAmount() != null) {
            BigDecimal newAmount = card.getUsedAmount().add(request.getAmount());
            if (newAmount.compareTo(card.getCreditLimit()) > 0) {
                return Mono.error(new BadRequestException("There are not enough funds to complete the transaction"));
            }
        }

        return Mono.just(tuple);
    }

    /**
     * Saves the transaction and updates the card's used amount
     * @param tuple Contains the transaction request and card details
     * @return The saved credit card transaction
     */
    private Mono<CreditCardTransaction> processTransaction(Tuple2<CreditCardTransactionRequest, CreditCardResponse> tuple) {
        CreditCardTransactionRequest request = tuple.getT1();
        CreditCardResponse card = tuple.getT2();

        return creditCardTransactionRepository.save(transactionMapper.getCreditCardTransactionEntity(request))
                .flatMap(transaction -> updateCreditCardAmount(card, request.getAmount())
                        .thenReturn(transaction));
    }

    private Mono<CreditCardResponse> updateCreditCardAmount(CreditCardResponse card, BigDecimal amount) {
        CreditCardPatchRequest patchRequest = new CreditCardPatchRequest();
        if (card.getUsedAmount() != null) {
            patchRequest.setUsedAmount(card.getUsedAmount().add(amount));
        }
        return creditCardService.updateCreditCard(card.getId(), patchRequest);
    }

    /**
     * Validate if the account exists and if it has available monthly movements
     *
     * @param req Transaction request
     * @return Mono of Transaction
     */
    private Mono<AccountTransactionRequest> validateAccount(AccountTransactionRequest req) {
        return accountService.findAccountByAccountNumber(req.getDestinationAccountNumber())
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
     * Process transaction by finding account and creating transaction entity
     *
     * @param req Transaction request
     * @return Mono of Transaction
     */
    private Mono<Transaction> processTransaction(AccountTransactionRequest req) {
        return accountService.findAccountByAccountNumber(req.getDestinationAccountNumber())
                .map(account -> Tuples.of(transactionMapper.getAccountTransactionEntity(req), account))
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
        AccountTransaction accountTransaction = (AccountTransaction) transaction;
        AccountTransactionType transactionType = AccountTransactionType.valueOf(accountTransaction.getType().toString());
        if (transactionType.equals(AccountTransactionType.DEPOSIT)) {
            newBalance = newBalance.add(accountTransaction.getAmount());
        } else {
            newBalance = newBalance.subtract(accountTransaction.getAmount());
        }
        Integer newMovements = accountResponse.getMonthlyMovements() + 1;
        accountPatchRequest.setBalance(newBalance);
        accountPatchRequest.setMonthlyMovements(newMovements);
        return accountPatchRequest;
    }


}
