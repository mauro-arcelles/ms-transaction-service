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
import com.project1.ms_transaction_service.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AccountTransactionMapper {
    public AccountTransactionResponse getAccountTransactionResponse(Transaction transaction) {
        AccountTransactionResponse response = new AccountTransactionResponse();
        AccountTransaction accountTransaction = (AccountTransaction) transaction;
        response.setId(transaction.getId());
        response.setOriginAccountNumber(accountTransaction.getOriginAccountNumber());
        response.setDestinationAccountNumber(accountTransaction.getDestinationAccountNumber());
        response.setType(accountTransaction.getType().toString());
        response.setAmount(accountTransaction.getAmount());
        response.setDate(accountTransaction.getDate());
        response.setDescription(accountTransaction.getDescription());
        return response;
    }

    public Transaction getAccountTransactionEntity(AccountTransactionRequest request) {
        AccountTransaction accountTransaction = new AccountTransaction();
        accountTransaction.setOriginAccountNumber(request.getOriginAccountNumber());
        accountTransaction.setDestinationAccountNumber(request.getDestinationAccountNumber());
        accountTransaction.setType(AccountTransactionType.valueOf(request.getType()));
        accountTransaction.setAmount(request.getAmount());
        accountTransaction.setDate(LocalDateTime.now());
        accountTransaction.setDescription(request.getDescription());
        return accountTransaction;
    }

    public AccountPatchRequest getAccountPatchRequest(Transaction transaction, AccountResponse accountResponse, boolean isOrigin) {
        AccountPatchRequest request = new AccountPatchRequest();
        AccountTransaction accountTransaction = (AccountTransaction) transaction;

        TransactionStrategy strategy;
        AccountTransactionType type = AccountTransactionType.valueOf(accountTransaction.getType().toString());

        switch (type) {
            case DEPOSIT:
                strategy = new DepositStrategy();
                break;
            case WITHDRAWAL:
                strategy = new WithdrawalStrategy();
                break;
            case TRANSFER:
                strategy = new TransferStrategy(isOrigin);
                break;
            default:
                throw new IllegalArgumentException("Invalid transaction type");
        }

        BigDecimal amount = Optional.ofNullable(accountTransaction.getAmount()).orElse(BigDecimal.ZERO);
        BigDecimal currentBalance = Optional.ofNullable(accountResponse.getBalance()).orElse(BigDecimal.ZERO);

        request.setBalance(strategy.calculateBalance(currentBalance, amount));

        if (strategy.updateMovements()) {
            request.setMonthlyMovements(Optional.ofNullable(accountResponse.getMonthlyMovements()).orElse(0) + 1);
        }

        return request;
    }

}
