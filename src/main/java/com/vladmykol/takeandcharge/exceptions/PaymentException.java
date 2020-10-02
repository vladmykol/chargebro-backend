package com.vladmykol.takeandcharge.exceptions;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ToString
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
