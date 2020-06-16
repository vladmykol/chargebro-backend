package com.vladmykol.takeandcharge.exceptions;

public class UnsupportedFieldType extends MessageSerializationError {
    public UnsupportedFieldType(String message) {
        super(message);
    }
}
