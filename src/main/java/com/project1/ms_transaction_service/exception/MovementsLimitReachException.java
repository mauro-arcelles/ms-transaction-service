package com.project1.ms_transaction_service.exception;

public class MovementsLimitReachException extends RuntimeException {
    public MovementsLimitReachException(String message) {
        super(message);
    }
}
