package com.vladmykol.takeandcharge.exceptions;

public class UnknownCommand extends RuntimeException {
    public UnknownCommand(String message) {
        super(message);
    }
}
