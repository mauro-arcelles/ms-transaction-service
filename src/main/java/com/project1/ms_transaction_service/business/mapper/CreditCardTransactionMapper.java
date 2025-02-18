package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreditCardUsageTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardUsageTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class CreditCardTransactionMapper {
    public CreditCardUsageTransactionResponse getCreditCardTransactionResponse(Transaction transaction) {
        CreditCardUsageTransactionResponse creditCardTransactionResponse = new CreditCardUsageTransactionResponse();
        CreditCardTransaction creditCardTransaction = (CreditCardTransaction) transaction;
        creditCardTransactionResponse.setCreditCard(creditCardTransaction.getCreditCard());
        creditCardTransactionResponse.setAmount(creditCardTransaction.getAmount());
        creditCardTransactionResponse.setDescription(creditCardTransaction.getDescription());
        creditCardTransactionResponse.setCustomerId(creditCardTransaction.getCustomerId());
        return creditCardTransactionResponse;
    }

    public CreditCardTransaction getCreditCardTransactionEntity(CreditCardUsageTransactionRequest request) {
        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setAmount(request.getAmount());
        creditCardTransaction.setCreditCard(request.getCreditCard());
        creditCardTransaction.setDescription(request.getDescription());
        creditCardTransaction.setCustomerId(request.getCustomerId());
        return creditCardTransaction;
    }
}
