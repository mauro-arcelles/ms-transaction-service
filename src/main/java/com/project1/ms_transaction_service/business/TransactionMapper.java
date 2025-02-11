package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.AccountTransactionRequest;
import com.project1.ms_transaction_service.model.AccountTransactionResponse;
import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import com.project1.ms_transaction_service.model.entity.AccountTransaction;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.Transaction;
import com.project1.ms_transaction_service.model.entity.AccountTransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {

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

    public CreditCardTransactionResponse getCreditCardTransactionResponse(Transaction transaction) {
        CreditCardTransactionResponse creditCardTransactionResponse = new CreditCardTransactionResponse();
        CreditCardTransaction creditCardTransaction = (CreditCardTransaction) transaction;
        creditCardTransactionResponse.setCreditCard(creditCardTransaction.getCreditCard());
        creditCardTransactionResponse.setAmount(creditCardTransaction.getAmount());
        creditCardTransactionResponse.setDescription(creditCardTransaction.getDescription());
        creditCardTransactionResponse.setCustomerId(creditCardTransaction.getCustomerId());
        return creditCardTransactionResponse;
    }

    public CreditCardTransaction getCreditCardTransactionEntity(CreditCardTransactionRequest request) {
        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setAmount(request.getAmount());
        creditCardTransaction.setCreditCard(request.getCreditCard());
        creditCardTransaction.setDescription(request.getDescription());
        creditCardTransaction.setCustomerId(request.getCustomerId());
        return creditCardTransaction;
    }

}