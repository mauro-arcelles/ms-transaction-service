package com.project1.ms_transaction_service.exception;

public class CreditCardCustomerMismatchException extends RuntimeException {
    public CreditCardCustomerMismatchException() {
        super("CREDIT CARD does not belong to provided CUSTOMER");
    }
}
