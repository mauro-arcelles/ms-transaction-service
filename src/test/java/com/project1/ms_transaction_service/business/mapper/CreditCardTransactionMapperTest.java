package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import com.project1.ms_transaction_service.model.entity.CreditCardTransaction;
import com.project1.ms_transaction_service.model.entity.CreditCardTransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CreditCardTransactionMapperTest {

    @Autowired
    private CreditCardTransactionMapper mapper;

    @Test
    void getCreditCardUsageTransactionEntity_ShouldMapAllFields() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setCreditCardId("123");
        request.setDescription("Test usage");
        request.setCustomerId("456");

        CreditCardTransaction transaction = mapper.getCreditCardUsageTransactionEntity(request);

        assertEquals(0, request.getAmount().compareTo(transaction.getAmount()));
        assertEquals(request.getCreditCardId(), transaction.getCreditCardId());
        assertEquals(request.getDescription(), transaction.getDescription());
        assertEquals(request.getCustomerId(), transaction.getCustomerId());
        assertEquals(CreditCardTransactionType.USAGE, transaction.getType());
        assertNotNull(transaction.getDate());
    }

    @Test
    void getCreditCardTransactionResponse_ShouldMapAllFields() {
        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setAmount(BigDecimal.valueOf(200));
        transaction.setCreditCardId("789");
        transaction.setDescription("Test response");
        transaction.setCustomerId("012");
        transaction.setType(CreditCardTransactionType.USAGE);
        transaction.setDate(LocalDateTime.now());

        CreditCardTransactionResponse response = mapper.getCreditCardTransactionResponse(transaction);

        assertEquals(transaction.getCreditCardId(), response.getCreditCard());
        assertEquals(0, transaction.getAmount().compareTo(response.getAmount()));
        assertEquals(transaction.getDescription(), response.getDescription());
        assertEquals(transaction.getCustomerId(), response.getCustomerId());
        assertEquals(transaction.getType().toString(), response.getType());
        assertEquals(transaction.getDate(), response.getDate());
    }

    @Test
    void getCreditCardPaymentTransactionEntity_ShouldMapAllFields() {
        CreditCardTransactionRequest request = new CreditCardTransactionRequest();
        request.setAmount(BigDecimal.valueOf(300));
        request.setCreditCardId("345");
        request.setDescription("Test payment");
        request.setCustomerId("678");

        CreditCardTransaction transaction = mapper.getCreditCardPaymentTransactionEntity(request);

        assertEquals(0, request.getAmount().compareTo(transaction.getAmount()));
        assertEquals(request.getCreditCardId(), transaction.getCreditCardId());
        assertEquals(request.getDescription(), transaction.getDescription());
        assertEquals(request.getCustomerId(), transaction.getCustomerId());
        assertEquals(CreditCardTransactionType.PAYMENT, transaction.getType());
        assertNotNull(transaction.getDate());
    }
}
