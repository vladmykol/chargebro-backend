package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.ErrorResponse;
import com.vladmykol.takeandcharge.exceptions.PaymentException;
import com.vladmykol.takeandcharge.exceptions.RentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        final ErrorResponse passwordIsIncorrect = new ErrorResponse(HttpStatus.FORBIDDEN,
                "User or password is incorrect");
        return new ResponseEntity<>(passwordIsIncorrect, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({RentException.class})
    public ResponseEntity<ErrorResponse> handleRentException(RentException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex), ex.getStatus());
    }

    @ExceptionHandler({PaymentException.class})
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.PAYMENT_REQUIRED, ex.getMessage()), HttpStatus.PAYMENT_REQUIRED);
    }
}
