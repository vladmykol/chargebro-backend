package com.vladmykol.takeandcharge.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }

    public static PaymentException noValidPaymentMethod() {
        return new PaymentException("Please add at least one valid payment card");
    }
}
