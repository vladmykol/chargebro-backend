package com.vladmykol.takeandcharge.exceptions;

public class RentAlreadyInProgress extends RuntimeException {
    public RentAlreadyInProgress() {
        super("You already have a pending rent in progress");
    }
}
