package com.vladmykol.takeandcharge.dto;

import com.vladmykol.takeandcharge.exceptions.RentException;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    private int status;
    private String error;
    private String message;

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
