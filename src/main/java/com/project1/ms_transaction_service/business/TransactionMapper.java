package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.TransactionRequest;
import com.project1.ms_transaction_service.model.TransactionResponse;
import com.project1.ms_transaction_service.model.entity.Transaction;
import com.project1.ms_transaction_service.model.entity.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class TransactionMapper {

    public TransactionResponse getTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setOriginAccountNumber(transaction.getOriginAccountNumber());
        response.setDestinationAccountNumber(transaction.getDestinationAccountNumber());
        response.setType(transaction.getType().toString());
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));
        response.setDescription(transaction.getDescription());
        return response;
    }

    public Transaction getTransactionEntity(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setOriginAccountNumber(request.getOriginAccountNumber());
        transaction.setDestinationAccountNumber(request.getDestinationAccountNumber());
        transaction.setType(TransactionType.valueOf(request.getType()));
        transaction.setAmount(request.getAmount());
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription(request.getDescription());
        return transaction;
    }
}