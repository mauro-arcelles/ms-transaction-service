package com.project1.ms_transaction_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@TypeAlias("bootcoinTransaction")
public class BootcoinTransaction extends Transaction {
    private String originWalletId;

    private String destinationWalletId;

    private WalletTransactionType type;

    private BigDecimal amount;
}
