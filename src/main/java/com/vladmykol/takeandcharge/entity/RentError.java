package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Objects;


@Data
public class RentError {
    @NonNull
    private final HttpStatus code;

    @NonNull
    private final String message;

    @NonNull
    private final String cause;

    private String stackTrace;

    public RentError(HttpStatus code, String message, Exception e) {
        this.code = code;
        this.message = Objects.requireNonNullElse(message, "Not defined error message");
        if (e.getCause() != null) {
            this.cause = e.getCause().toString();
        } else {
            this.cause = e.toString();
        }
        if (code.is5xxServerError()) {
            this.stackTrace = Arrays.toString(e.getStackTrace());
        }
    }
}
