package com.example.accountservice.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException user(Object id) {
        return new ResourceNotFoundException("User not found with id: " + id);
    }

    public static ResourceNotFoundException account(Object id) {
        return new ResourceNotFoundException("Account not found with id: " + id);
    }

    public static ResourceNotFoundException accountByNumber(String number) {
        return new ResourceNotFoundException("Account not found with number: " + number);
    }

    public static ResourceNotFoundException transaction(Object id) {
        return new ResourceNotFoundException("Transaction not found with id: " + id);
    }
}
