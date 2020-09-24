package com.vladmykol.takeandcharge.exceptions;

public class PaymentGatewayException extends PaymentException {
    public PaymentGatewayException(String message) {
        super(message);
    }
}
