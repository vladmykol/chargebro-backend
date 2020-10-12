package com.vladmykol.takeandcharge.exceptions;

public class NotSuccessesRent extends StationCommunicatingException {
    public NotSuccessesRent() {
        super("Station responded with not successful rent operation");
    }
}
