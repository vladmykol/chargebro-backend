package com.vladmykol.takeandcharge.exceptions;

public class NoResponseFromWithinTimeout extends ChargingStationException {
    public NoResponseFromWithinTimeout(int timeout) {
        super("No response from a station socket client within timeout of " + timeout + " ms");
    }
}
