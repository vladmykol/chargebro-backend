package com.vladmykol.takeandcharge.exceptions;

public class IncompatibleFieldType extends StationCommunicatingException {
    public IncompatibleFieldType(String message, Throwable cause) {
        super(message, cause);
    }
}
