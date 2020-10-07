package com.vladmykol.takeandcharge.exceptions;

public class NoHandlerDefined extends ChargingStationException {
    public NoHandlerDefined(short command) {
        super("cannot map command " + command + " to any handler");
    }
}
