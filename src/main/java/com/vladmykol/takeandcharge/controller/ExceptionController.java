package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.ErrorResponse;
import com.vladmykol.takeandcharge.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionController extends ResponseEntityExceptionHandler {
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        log.warn("User or password is bad when login via rest api", e);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN,
                "User or password is incorrect");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<ErrorResponse> responseStatusException(ResponseStatusException ex) {
        log.debug("Response error from rest api", ex);
        final ErrorResponse errorResponse = new ErrorResponse(ex.getStatus(),
                ex.getMessage());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }


    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> generalException(Exception ex) {
        log.error("Error in rest api", ex);
        final var rentException = ExceptionUtil.convertToHttpException(ex);

        return new ResponseEntity<>(new ErrorResponse(rentException), rentException.getStatus());
    }
}

