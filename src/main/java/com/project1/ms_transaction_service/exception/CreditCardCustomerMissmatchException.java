package com.project1.ms_transaction_service.exception;

public class CreditCardCustomerMissmatchException extends RuntimeException {
    public CreditCardCustomerMissmatchException() {
        super("Credit card does not belongs to provided customer");
    }
}
