package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.DebitCardTransactionRequest;
import com.project1.ms_transaction_service.model.DebitCardTransactionResponse;
import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import com.project1.ms_transaction_service.model.entity.DebitCardTransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DebitCardTransactionMapper {
    public DebitCardTransaction getDebitCardTransactionEntity(DebitCardTransactionRequest request) {
        return DebitCardTransaction.builder()
            .debitCardId(request.getDebitCardId())
            .amount(request.getAmount())
            .date(LocalDateTime.now())
            .description(request.getDescription())
            .type(DebitCardTransactionType.valueOf(request.getType()))
            .build();
    }

    public DebitCardTransactionResponse getDebitCardTransactionResponse(DebitCardTransaction transaction) {
        DebitCardTransactionResponse response = new DebitCardTransactionResponse();
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate());
        response.setDescription(transaction.getDescription());
        response.setDebitCardId(transaction.getDebitCardId());
        response.setCustomerId(transaction.getCustomerId());
        response.setType(transaction.getType().toString());
        return response;
    }
}
