package com.vladmykol.takeandcharge.exceptions;

public class UnknownCommand extends StationCommunicatingException {
    public UnknownCommand(String message) {
        super(message);
    }
}
