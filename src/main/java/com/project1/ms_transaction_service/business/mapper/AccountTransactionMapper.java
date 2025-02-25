package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.business.service.strategy.DepositStrategy;
import com.project1.ms_transaction_service.business.service.strategy.TransactionStrategy;
import com.project1.ms_transaction_service.business.service.strategy.TransferStrategy;
import com.project1.ms_transaction_service.business.service.strategy.WithdrawalStrategy;
import com.project1.ms_transaction_service.model.AccountPatchRequest;
import com.project1.ms_transaction_service.model.AccountResponse;
import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.model.entity.AccountTransactionType;
import com.project1.ms_transaction_service.model.entity.AccountType;
import com.project1.ms_transaction_service.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AccountTransactionMapper {
    public AccountTransactionResponse getAccountTransactionResponse(AccountTransaction transaction) {
        AccountTransactionResponse response = new AccountTransactionResponse();
        response.setId(transaction.getId());
        response.setOriginAccountNumber(transaction.getOriginAccountNumber());
        response.setDestinationAccountNumber(transaction.getDestinationAccountNumber());
        response.setType(transaction.getType().toString());
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate());
        response.setDescription(transaction.getDescription());
        return response;
    }

    /**
     * Creates an AccountTransaction entity from a request and account response
     * @param request The transaction request containing transaction details
     * @param accountResponse The account information response
     * @return A new AccountTransaction entity
     */
    public AccountTransaction getAccountTransactionEntity(AccountTransactionRequest request, AccountResponse accountResponse) {
        AccountTransaction accountTransaction = createBaseTransaction(request);

        if (AccountTransactionType.valueOf(request.getType()) == AccountTransactionType.TRANSFER) {
            accountTransaction.setDestinationAccountNumber(request.getDestinationAccountNumber());
        }

        if (shouldApplyCommissionFee(request, accountResponse)) {
            applyCommissionFee(accountTransaction, accountResponse, request.getAmount());
        }

        return accountTransaction;
    }

    /**
     * Creates base transaction with common fields
     * @param request Source request containing transaction details
     * @return Basic AccountTransaction with common fields set
     */
    private AccountTransaction createBaseTransaction(AccountTransactionRequest request) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setOriginAccountNumber(request.getOriginAccountNumber());
        transaction.setType(AccountTransactionType.valueOf(request.getType()));
        transaction.setAmount(request.getAmount());
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription(request.getDescription());
        return transaction;
    }

    /**
     * Determines if commission fee should be applied based on account and transaction type
     * @param request Transaction request details
     * @param accountResponse Account information
     * @return true if commission fee should be applied
     */
    private boolean shouldApplyCommissionFee(AccountTransactionRequest request, AccountResponse accountResponse) {
        AccountTransactionType transactionType = AccountTransactionType.valueOf(request.getType());
        AccountType accountType = AccountType.valueOf(accountResponse.getAccountType());

        return accountType == AccountType.SAVINGS
            && (transactionType == AccountTransactionType.DEPOSIT || transactionType == AccountTransactionType.WITHDRAWAL)
            && hasExceededFreeMovements(accountResponse);
    }

    /**
     * Checks if account has exceeded free monthly movements
     * @param accountResponse Account information containing movement limits
     * @return true if free movements exceeded
     */
    private boolean hasExceededFreeMovements(AccountResponse accountResponse) {
        return accountResponse.getMonthlyMovements() != null
            && accountResponse.getMaxMonthlyMovementsNoFee() != null
            && accountResponse.getTransactionCommissionFeePercentage() != null
            && accountResponse.getMonthlyMovements() >= accountResponse.getMaxMonthlyMovementsNoFee();
    }

    /**
     * Applies commission fee to transaction
     * @param transaction Transaction to apply fee to
     * @param accountResponse Account details containing fee information
     * @param amount Transaction amount
     */
    private void applyCommissionFee(AccountTransaction transaction, AccountResponse accountResponse, BigDecimal amount) {
        BigDecimal feePercentage = accountResponse.getTransactionCommissionFeePercentage();
        transaction.setCommissionFeePercentage(feePercentage);
        transaction.setCommissionFee(calculateCommissionFee(feePercentage, amount));
    }

    /**
     * Calculates commission fee amount
     * @param feePercentage Fee percentage to apply
     * @param amount Amount to calculate fee from
     * @return Calculated commission fee
     */
    private BigDecimal calculateCommissionFee(BigDecimal feePercentage, BigDecimal amount) {
        return feePercentage
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            .multiply(amount);
    }

    public AccountPatchRequest getAccountPatchRequest(AccountTransaction transaction, AccountResponse accountResponse, boolean isOrigin) {
        AccountPatchRequest request = new AccountPatchRequest();

        TransactionStrategy strategy;
        AccountTransactionType type = AccountTransactionType.valueOf(transaction.getType().toString());

        switch (type) {
            case DEPOSIT:
                strategy = new DepositStrategy(isOrigin, transaction.getCommissionFee());
                break;
            case WITHDRAWAL:
                strategy = new WithdrawalStrategy(isOrigin, transaction.getCommissionFee());
                break;
            case TRANSFER:
                strategy = new TransferStrategy(isOrigin);
                break;
            default:
                throw new IllegalArgumentException("Invalid transaction type");
        }

        BigDecimal amount = Optional.ofNullable(transaction.getAmount()).orElse(BigDecimal.ZERO);
        BigDecimal currentBalance = Optional.ofNullable(accountResponse.getBalance()).orElse(BigDecimal.ZERO);

        request.setBalance(strategy.calculateBalance(currentBalance, amount));

        if (strategy.updateMovements()) {
            request.setMonthlyMovements(Optional.ofNullable(accountResponse.getMonthlyMovements()).orElse(0) + 1);
        }

        return request;
    }

}
