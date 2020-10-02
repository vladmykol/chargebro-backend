package com.vladmykol.takeandcharge.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class RentException extends RuntimeException {
    @Getter
    private final HttpStatus status;

    public RentException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
