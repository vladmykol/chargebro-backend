package com.vladmykol.takeandcharge.exceptions;

public class PaymentGatewaySignatureException extends PaymentGatewayException {
    public PaymentGatewaySignatureException(String message) {
        super(message);
    }
}
