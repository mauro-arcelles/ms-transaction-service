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

    public AccountTransaction getAccountTransactionEntity(AccountTransactionRequest request, AccountResponse accountResponse) {
        AccountTransaction accountTransaction = new AccountTransaction();
        accountTransaction.setOriginAccountNumber(request.getOriginAccountNumber());
        AccountTransactionType accountTransactionType = AccountTransactionType.valueOf(request.getType());
        if (accountTransactionType == AccountTransactionType.TRANSFER) {
            accountTransaction.setDestinationAccountNumber(request.getDestinationAccountNumber());
        }
        AccountType accountType = AccountType.valueOf(accountResponse.getAccountType());
        if (accountType == AccountType.SAVINGS) {
            if (accountTransactionType == AccountTransactionType.DEPOSIT || accountTransactionType == AccountTransactionType.WITHDRAWAL) {
                if (accountResponse.getMonthlyMovements() != null
                        && accountResponse.getMaxMonthlyMovementsNoFee() != null
                        && accountResponse.getTransactionCommissionFeePercentage() != null
                        && accountResponse.getMonthlyMovements() >= accountResponse.getMaxMonthlyMovementsNoFee()) {

                    accountTransaction.setCommissionFeePercentage(accountResponse.getTransactionCommissionFeePercentage());
                    BigDecimal commissionFee = accountResponse.getTransactionCommissionFeePercentage()
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                            .multiply(request.getAmount());
                    accountTransaction.setCommissionFee(commissionFee);
                }
            }
        }
        accountTransaction.setType(AccountTransactionType.valueOf(request.getType()));
        accountTransaction.setAmount(request.getAmount());
        accountTransaction.setDate(LocalDateTime.now());
        accountTransaction.setDescription(request.getDescription());
        return accountTransaction;
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
