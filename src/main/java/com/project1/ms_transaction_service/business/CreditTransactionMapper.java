package com.project1.ms_transaction_service.business;

import com.project1.ms_transaction_service.model.CreditPaymentTransactionRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import org.springframework.stereotype.Component;

@Component
public class CreditTransactionMapper {
    public CreditTransaction getCreditPaymentTransactionEntity(CreditPaymentTransactionRequest request) {
        CreditTransaction creditTransaction = new CreditTransaction();
        creditTransaction.setCreditId(request.getCreditId());
        return creditTransaction;
    }

    public CreditPaymentTransactionResponse getCreditPaymentTransactionResponse(CreditTransaction creditTransaction) {
        CreditPaymentTransactionResponse creditPaymentTransactionResponse = new CreditPaymentTransactionResponse();
        creditPaymentTransactionResponse.setCreditId(creditTransaction.getCreditId());
        return  creditPaymentTransactionResponse;
    }
}
