package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreditPaymentTransactionRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import com.project1.ms_transaction_service.model.entity.CreditTransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreditTransactionMapper {
    public CreditTransaction getCreditPaymentTransactionEntity(CreditPaymentTransactionRequest request) {
        CreditTransaction creditTransaction = new CreditTransaction();
        creditTransaction.setCreditId(request.getCreditId());
        creditTransaction.setDate(LocalDateTime.now());
        creditTransaction.setCustomerId(request.getCustomerId());
        creditTransaction.setType(CreditTransactionType.PAYMENT);
        return creditTransaction;
    }

    public CreditPaymentTransactionResponse getCreditPaymentTransactionResponse(CreditTransaction creditTransaction) {
        CreditPaymentTransactionResponse creditPaymentTransactionResponse = new CreditPaymentTransactionResponse();
        creditPaymentTransactionResponse.setCreditId(creditTransaction.getCreditId());
        creditPaymentTransactionResponse.setDate(creditTransaction.getDate());
        creditPaymentTransactionResponse.setCustomerId(creditTransaction.getCustomerId());
        creditPaymentTransactionResponse.setType(creditTransaction.getType().toString());
        return  creditPaymentTransactionResponse;
    }
}
