package com.project1.ms_transaction_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@TypeAlias("creditTransaction")
public class CreditTransaction extends Transaction {
    private String creditId;

    private String customerId;

    private CreditTransactionType type;
}
