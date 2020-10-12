package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.exceptions.HttpException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;


@Data
@NoArgsConstructor
public class RentError {
    @NonNull
    private HttpStatus status;

    @NonNull
    private String message;

    private String stackTrace;

    public RentError(HttpException rentException) {
        this.status = rentException.getStatus();
        this.message = rentException.getMessage();
        if (status.is5xxServerError()) {
            this.stackTrace = Arrays.toString(rentException.getStackTrace());
        }
    }
}
