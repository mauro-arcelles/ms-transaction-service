package com.project1.ms_transaction_service.business.service.strategy;

import java.math.BigDecimal;

public class DepositStrategy implements TransactionStrategy {
    private final BigDecimal commissionFee;

    private final boolean isOrigin;

    public DepositStrategy(boolean isOrigin, BigDecimal commissionFee) {
        this.commissionFee = commissionFee;
        this.isOrigin = isOrigin;
    }

    public BigDecimal calculateBalance(BigDecimal current, BigDecimal amount) {
        BigDecimal finalAmount = amount;
        if (commissionFee != null && isOrigin) {
            finalAmount = amount.subtract(commissionFee);
        }
        return current.add(finalAmount);
    }

    public boolean updateMovements() {
        return true;
    }
}
