package com.vladmykol.takeandcharge.utils;


import com.vladmykol.takeandcharge.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class ExceptionUtil {

    public static HttpException convertToHttpException(Exception e) {
        HttpException rentException;
        if (e instanceof PaymentException) {
            rentException = new HttpException(HttpStatus.PAYMENT_REQUIRED, e);
        } else if (e instanceof CabinetIsOffline || e instanceof NoPowerBanksLeft) {
            rentException = new HttpException(HttpStatus.PRECONDITION_FAILED, e);
        } else if (e instanceof StationCommunicatingException) {
            rentException = new HttpException(HttpStatus.SERVICE_UNAVAILABLE, e);
        } else if (e instanceof SmsSendingError) {
            rentException = new HttpException(HttpStatus.PRECONDITION_FAILED, e);
        } else if (e instanceof UserAlreadyExist) {
            rentException = new HttpException(HttpStatus.CONFLICT, e);
        } else if (e instanceof UserIsBlocked) {
            rentException = new HttpException(HttpStatus.PRECONDITION_FAILED, e);
        } else if (e instanceof UserIsFrozen) {
            rentException = new HttpException(HttpStatus.EXPECTATION_FAILED, e);
        } else {
            rentException = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        return rentException;
    }

}

