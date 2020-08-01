package com.vladmykol.takeandcharge.exceptions;

public class SmsSendingError extends RuntimeException {
    public SmsSendingError(String message) {
        super(message);
    }

    public SmsSendingError() {
        super("Validation SMS was not send. Please contact support");
    }
}
