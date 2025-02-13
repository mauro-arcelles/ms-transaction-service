package com.project1.ms_transaction_service.business;

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

    public AccountPatchRequest getAccountPatchRequest(Transaction transaction, AccountResponse accountResponse) {
        AccountPatchRequest accountPatchRequest = new AccountPatchRequest();
        BigDecimal newBalance = Optional.ofNullable(accountResponse.getBalance()).orElse(BigDecimal.ZERO);
        AccountTransaction accountTransaction = (AccountTransaction) transaction;
        AccountTransactionType transactionType = AccountTransactionType.valueOf(accountTransaction.getType().toString());

        if (transactionType.equals(AccountTransactionType.DEPOSIT)) {
            newBalance = newBalance.add(Optional.ofNullable(accountTransaction.getAmount()).orElse(BigDecimal.ZERO));
        } else {
            newBalance = newBalance.subtract(Optional.ofNullable(accountTransaction.getAmount()).orElse(BigDecimal.ZERO));
        }

        Integer newMovements = Optional.ofNullable(accountResponse.getMonthlyMovements()).orElse(0) + 1;
        accountPatchRequest.setBalance(newBalance);
        accountPatchRequest.setMonthlyMovements(newMovements);
        return accountPatchRequest;
    }

}
