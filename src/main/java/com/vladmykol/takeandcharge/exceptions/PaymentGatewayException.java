package com.vladmykol.takeandcharge.exceptions;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }
}
