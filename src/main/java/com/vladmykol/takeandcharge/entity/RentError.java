package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.exceptions.RentException;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Arrays;


@Data
public class RentError {
    @NonNull
    private final HttpStatus status;

    @NonNull
    private final String message;

    private final String stackTrace;

    public RentError(RentException rentException) {
        this.status = rentException.getStatus();
        if (rentException.getCause().getMessage() != null) {
            this.message = rentException.getCause().getMessage();
        } else {
            this.message = rentException.getCause().getClass().getSimpleName();
        }
        if (status.is5xxServerError()) {
            this.stackTrace = Arrays.toString(rentException.getStackTrace());
        } else {
            this.stackTrace = null;
        }
    }
}
