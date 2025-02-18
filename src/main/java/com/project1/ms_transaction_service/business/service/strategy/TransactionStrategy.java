package com.project1.ms_transaction_service.business.service.strategy;

import java.math.BigDecimal;

public interface TransactionStrategy {
    BigDecimal calculateBalance(BigDecimal current, BigDecimal amount);
    boolean updateMovements();
}
