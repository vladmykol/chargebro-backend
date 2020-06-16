package com.vladmykol.takeandcharge.exceptions;

public class IncompatibleFieldType extends MessageSerializationError {
    public IncompatibleFieldType(String message, Throwable cause) {
        super(message, cause);
    }
}
