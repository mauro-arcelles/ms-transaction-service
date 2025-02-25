package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.DebitCardTransactionRequest;
import com.project1.ms_transaction_service.model.DebitCardTransactionResponse;
import com.project1.ms_transaction_service.model.entity.DebitCardTransaction;
import com.project1.ms_transaction_service.model.entity.DebitCardTransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DebitCardTransactionMapperTest {

    @Autowired
    private DebitCardTransactionMapper mapper;

    @Test
    void getDebitCardTransactionEntity_ShouldMapAllFields() {
        DebitCardTransactionRequest request = new DebitCardTransactionRequest();
        request.setDebitCardId("123");
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transaction");
        request.setType(DebitCardTransactionType.PURCHASE.toString());

        DebitCardTransaction transaction = mapper.getDebitCardTransactionEntity(request);

        assertEquals(request.getDebitCardId(), transaction.getDebitCardId());
        assertEquals(0, request.getAmount().compareTo(transaction.getAmount()));
        assertEquals(request.getDescription(), transaction.getDescription());
        assertEquals(DebitCardTransactionType.valueOf(request.getType()), transaction.getType());
        assertNotNull(transaction.getDate());
    }

    @Test
    void getDebitCardTransactionResponse_ShouldMapAllFields() {
        DebitCardTransaction transaction = DebitCardTransaction.builder()
            .debitCardId("456")
            .amount(BigDecimal.valueOf(200))
            .date(LocalDateTime.now())
            .description("Test response")
            .type(DebitCardTransactionType.PURCHASE)
            .customerId("789")
            .build();

        DebitCardTransactionResponse response = mapper.getDebitCardTransactionResponse(transaction);

        assertEquals(0, transaction.getAmount().compareTo(response.getAmount()));
        assertEquals(transaction.getDate(), response.getDate());
        assertEquals(transaction.getDescription(), response.getDescription());
        assertEquals(transaction.getDebitCardId(), response.getDebitCardId());
        assertEquals(transaction.getCustomerId(), response.getCustomerId());
        assertEquals(transaction.getType().toString(), response.getType());
    }
}
