package com.project1.ms_transaction_service.business.mapper;

import com.project1.ms_transaction_service.model.CreateWalletTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionRequest;
import com.project1.ms_transaction_service.model.CreditCardTransactionResponse;
import com.project1.ms_transaction_service.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class WalletTransactionMapper {
    @Autowired
    private Clock clock;

    public WalletTransaction getWalletTransactionEntity(CreateWalletTransactionRequest request) {
        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setAmount(request.getAmount());
        walletTransaction.setOriginWalletId(request.getOriginWalletId());
        walletTransaction.setDestinationWalletId(request.getDestinationWalletId());
        walletTransaction.setType(request.getType());
        walletTransaction.setDate(LocalDateTime.now(clock));
        return walletTransaction;
    }
}
