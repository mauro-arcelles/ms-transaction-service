package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.CreditCardTransactionType;
import com.project1.ms_transaction_service.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreditCardTransactionMapper {
    public CreditCardTransaction getCreditCardUsageTransactionEntity(CreditCardTransactionRequest request) {
        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setAmount(request.getAmount());
        creditCardTransaction.setCreditCard(request.getCreditCardId());
        creditCardTransaction.setDescription(request.getDescription());
        creditCardTransaction.setCustomerId(request.getCustomerId());
        creditCardTransaction.setType(CreditCardTransactionType.USAGE);
        creditCardTransaction.setDate(LocalDateTime.now());
        return creditCardTransaction;
    }

    public CreditCardTransactionResponse getCreditCardTransactionResponse(Transaction transaction) {
        CreditCardTransactionResponse creditCardTransactionResponse = new CreditCardTransactionResponse();
        CreditCardTransaction creditCardTransaction = (CreditCardTransaction) transaction;
        creditCardTransactionResponse.setCreditCard(creditCardTransaction.getCreditCard());
        creditCardTransactionResponse.setAmount(creditCardTransaction.getAmount());
        creditCardTransactionResponse.setDescription(creditCardTransaction.getDescription());
        creditCardTransactionResponse.setCustomerId(creditCardTransaction.getCustomerId());
        creditCardTransactionResponse.setType(creditCardTransaction.getType().toString());
        creditCardTransactionResponse.setDate(transaction.getDate());
        return creditCardTransactionResponse;
    }

    public CreditCardTransaction getCreditCardPaymentTransactionEntity(CreditCardTransactionRequest request) {
        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setAmount(request.getAmount());
        creditCardTransaction.setCreditCard(request.getCreditCardId());
        creditCardTransaction.setDescription(request.getDescription());
        creditCardTransaction.setCustomerId(request.getCustomerId());
        creditCardTransaction.setType(CreditCardTransactionType.PAYMENT);
        creditCardTransaction.setDate(LocalDateTime.now());
        return creditCardTransaction;
    }
}
