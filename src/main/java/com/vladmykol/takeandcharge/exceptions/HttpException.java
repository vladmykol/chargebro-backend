package com.vladmykol.takeandcharge.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class HttpException extends RuntimeException {
    @Getter
    private final HttpStatus status;

    public HttpException(HttpStatus status, Exception e) {
        super(e);
        this.status = status;
    }

    @Override
    public String getMessage() {
        if (getCause().getMessage() != null) {
            return getCause().getMessage();
        } else {
            return getCause().getClass().getSimpleName();
        }
    }
}
