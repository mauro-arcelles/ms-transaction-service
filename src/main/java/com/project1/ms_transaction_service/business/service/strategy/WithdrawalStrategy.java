package com.project1.ms_transaction_service.business.service.strategy;

import java.math.BigDecimal;

public class WithdrawalStrategy implements TransactionStrategy {
    private final BigDecimal commissionFee;
    private final boolean isOrigin;

    public WithdrawalStrategy(boolean isOrigin, BigDecimal commissionFee) {
        this.commissionFee = commissionFee;
        this.isOrigin = isOrigin;
    }

    public BigDecimal calculateBalance(BigDecimal current, BigDecimal amount) {
        BigDecimal finalAmount = amount;
        if (commissionFee != null && isOrigin) {
            finalAmount = amount.add(commissionFee);
        }
        return current.subtract(finalAmount);
    }
    public boolean updateMovements() { return true; }
}