package com.vladmykol.takeandcharge.dto;

import com.vladmykol.takeandcharge.exceptions.RentException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(RentException ex) {
        this.status = ex.getStatus().value();
        this.error = ex.getStatus().name();
        if (ex.getCause().getMessage() != null) {
            this.message = ex.getCause().getMessage();
        } else {
            this.message = ex.getCause().getClass().getSimpleName();
        }

    }

    public ErrorResponse(HttpStatus status, String message, Exception e) {
        this.status = status.value();
        this.error = status.name();
        this.message = message;
    }
}
