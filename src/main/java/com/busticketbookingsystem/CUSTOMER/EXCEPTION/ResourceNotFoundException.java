package com.busticketbookingsystem.CUSTOMER.EXCEPTION;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}