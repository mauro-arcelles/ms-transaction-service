package com.project1.ms_transaction_service.business.service.strategy;

import java.math.BigDecimal;

public class DepositStrategy implements TransactionStrategy {
    public BigDecimal calculateBalance(BigDecimal current, BigDecimal amount) {
        return current.add(amount);
    }
    public boolean updateMovements() { return true; }
}
