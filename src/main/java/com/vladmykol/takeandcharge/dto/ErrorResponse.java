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
        this.message = ex.getMessage();
    }

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.error = status.name();
        this.message = message;
    }
}
