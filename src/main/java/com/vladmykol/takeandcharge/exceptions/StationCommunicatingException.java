package com.vladmykol.takeandcharge.exceptions;

public abstract class StationCommunicatingException extends ChargingStationException {
    public StationCommunicatingException(Throwable cause) {
        super(cause);
    }

    public StationCommunicatingException(String message, Throwable cause) {
        super(message, cause);
    }

    public StationCommunicatingException(String message) {
        super(message);
    }

    public StationCommunicatingException() {
        super();
    }
}
