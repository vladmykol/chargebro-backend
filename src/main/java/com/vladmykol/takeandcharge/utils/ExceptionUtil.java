package com.vladmykol.takeandcharge.utils;


import com.vladmykol.takeandcharge.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ExceptionUtil {

    public static RentException convertToRentException(Exception e) {
        RentException rentException;
        if (e instanceof PaymentException) {
            rentException = new RentException(HttpStatus.PAYMENT_REQUIRED, e);
        } else if (e instanceof CabinetIsOffline || e instanceof NoPowerBanksLeft) {
            rentException = new RentException(HttpStatus.PRECONDITION_FAILED, e);
        } else if (e instanceof StationCommunicatingException) {
            rentException = new RentException(HttpStatus.SERVICE_UNAVAILABLE, e);
        } else {
            rentException = new RentException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        return rentException;
    }

}

