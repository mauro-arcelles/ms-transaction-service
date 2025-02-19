package com.project1.ms_transaction_service.business.service.strategy;

import java.math.BigDecimal;

public class TransferStrategy implements TransactionStrategy {
    private final boolean isOrigin;

    public TransferStrategy(boolean isOrigin) {
        this.isOrigin = isOrigin;
    }

    public BigDecimal calculateBalance(BigDecimal current, BigDecimal amount) {
        return isOrigin ? current.subtract(amount) : current.add(amount);
    }

    public boolean updateMovements() { return isOrigin; }
}
