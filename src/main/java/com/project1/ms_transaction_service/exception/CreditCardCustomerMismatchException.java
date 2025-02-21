package com.project1.ms_transaction_service.exception;

public class CreditCardCustomerMismatchException extends RuntimeException {
    public CreditCardCustomerMismatchException() {
        super("Credit card does not belong to provided customer");
    }
}
