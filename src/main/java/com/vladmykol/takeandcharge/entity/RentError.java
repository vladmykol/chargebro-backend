package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.exceptions.RentException;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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

    public RentError(RentException rentException) {
        this.status = rentException.getStatus();
        this.message = rentException.getMessage();
        if (status.is5xxServerError()) {
            this.stackTrace = Arrays.toString(rentException.getStackTrace());
        }
    }
}
