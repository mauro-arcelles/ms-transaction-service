package com.project1.ms_transaction_service.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String originAccountNumber;
    private String destinationAccountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
}