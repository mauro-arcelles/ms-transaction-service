package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreditPaymentTransactionRequest;
import com.project1.ms_transaction_service.model.CreditPaymentTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditTransaction;
import com.project1.ms_transaction_service.model.entity.CreditTransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CreditTransactionMapperTest {

    @Autowired
    private CreditTransactionMapper mapper;

    @Test
    void getCreditPaymentTransactionEntity_ShouldMapAllFields() {
        CreditPaymentTransactionRequest request = new CreditPaymentTransactionRequest();
        request.setCreditId("123");
        request.setCustomerId("456");

        CreditTransaction transaction = mapper.getCreditPaymentTransactionEntity(request);

        assertEquals(request.getCreditId(), transaction.getCreditId());
        assertEquals(request.getCustomerId(), transaction.getCustomerId());
        assertEquals(CreditTransactionType.PAYMENT, transaction.getType());
        assertNotNull(transaction.getDate());
    }

    @Test
    void getCreditPaymentTransactionResponse_ShouldMapAllFields() {
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCreditId("789");
        transaction.setCustomerId("012");
        transaction.setType(CreditTransactionType.PAYMENT);
        transaction.setDate(LocalDateTime.now());

        CreditPaymentTransactionResponse response = mapper.getCreditPaymentTransactionResponse(transaction);

        assertEquals(transaction.getCreditId(), response.getCreditId());
        assertEquals(transaction.getCustomerId(), response.getCustomerId());
        assertEquals(transaction.getType().toString(), response.getType());
        assertEquals(transaction.getDate(), response.getDate());
    }
}
