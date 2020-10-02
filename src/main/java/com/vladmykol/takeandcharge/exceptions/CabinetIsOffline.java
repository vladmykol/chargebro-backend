package com.vladmykol.takeandcharge.exceptions;

public class CabinetIsOffline extends ChargingStationException {
    public CabinetIsOffline() {
        super("Cabinet is offline");
    }
}
