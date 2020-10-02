package com.vladmykol.takeandcharge.exceptions;

public class UnsupportedFieldType extends StationCommunicatingException {
    public UnsupportedFieldType(String message) {
        super(message);
    }
}
