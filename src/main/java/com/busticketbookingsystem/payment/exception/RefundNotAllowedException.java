package com.busticketbookingsystem.payment.exception;


public class RefundNotAllowedException extends RuntimeException {

    public RefundNotAllowedException(String message) {
        super(message);
    }
}