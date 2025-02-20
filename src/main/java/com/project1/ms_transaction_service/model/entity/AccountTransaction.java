package com.project1.ms_transaction_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@TypeAlias("accountTransaction")
public class AccountTransaction extends Transaction {
    private String originAccountNumber;

    private String destinationAccountNumber;

    private AccountTransactionType type;

    private BigDecimal amount;

    private String description;

    private BigDecimal commissionFee;

    private BigDecimal commissionFeePercentage;
}
