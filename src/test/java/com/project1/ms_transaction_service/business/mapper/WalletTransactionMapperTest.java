package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import com.project1.ms_transaction_service.model.entity.WalletTransaction;
import com.project1.ms_transaction_service.model.entity.WalletTransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class WalletTransactionMapperTest {

    @Autowired
    private WalletTransactionMapper mapper;

    @Autowired
    private Clock clock;

    @Test
    void getWalletTransactionEntity_ShouldMapAllFields() {
        CreateWalletTransactionRequest request = new CreateWalletTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setOriginWalletId("wallet123");
        request.setDestinationWalletId("wallet456");
        request.setType(WalletTransactionType.TRANSFER.toString());

        WalletTransaction transaction = mapper.getWalletTransactionEntity(request);

        assertEquals(request.getAmount(), transaction.getAmount());
        assertEquals(request.getOriginWalletId(), transaction.getOriginWalletId());
        assertEquals(request.getDestinationWalletId(), transaction.getDestinationWalletId());
        assertEquals(request.getType(), transaction.getType());
        assertNotNull(transaction.getDate());
    }
}
