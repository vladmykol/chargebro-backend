package com.vladmykol.takeandcharge.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
public class PaymentValidationResult {
    private final int code;
    private final String message;

    public PaymentValidationResult(String message) {
        this.code = HttpStatus.OK.value();
        this.message = message;
    }
}
