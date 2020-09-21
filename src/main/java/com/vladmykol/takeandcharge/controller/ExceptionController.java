package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import com.vladmykol.takeandcharge.exceptions.NoPowerBanksLeft;
import com.vladmykol.takeandcharge.exceptions.NotSuccessesRent;
import com.vladmykol.takeandcharge.exceptions.RentIsNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {
    @ExceptionHandler({BadCredentialsException.class})
    public void handleBadCredentials(
            Exception ex,HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value(), "User or password is incorrect");
    }

    @ExceptionHandler({NotSuccessesRent.class})
    public void notSuccessesRent(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.EXPECTATION_FAILED.value(), "Station cannot give powerbank");
    }

    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class})
    public void authIssue(
            Exception ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    }

    @ExceptionHandler({CabinetIsOffline.class})
    public void cabinetIsOffline(Exception ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.PRECONDITION_FAILED.value(), "Station is offline");
    }

    @ExceptionHandler({NoPowerBanksLeft.class})
    public void noPowerBanksLeft(Exception ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.PRECONDITION_FAILED.value(), "No available power banks");
    }

    @ExceptionHandler({RentIsNotFound.class})
    public void notFoundRent(Exception ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.PRECONDITION_FAILED.value(), "No rent request found");
    }


}
