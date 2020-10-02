package com.vladmykol.takeandcharge.exceptions;

public abstract class ChargingStationException extends RuntimeException {
    public ChargingStationException(Throwable cause) {
        super(cause);
    }

    public ChargingStationException(String message) {
        super(message);
    }

    public ChargingStationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChargingStationException() {
        super();
    }
}
