package com.project1.ms_transaction_service.exception;

public class CreditCardCustomerMismatchException extends RuntimeException {
    public CreditCardCustomerMismatchException() {
        super("Credit card does not belongs to provided customer");
    }
}
