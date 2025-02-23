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
@TypeAlias("debitCardTransaction")
public class DebitCardTransaction extends Transaction {
    private String debitCardId;

    private String accountId;

    private String customerId;

    private BigDecimal amount;

    private String description;

    private DebitCardTransactionType type;
}
