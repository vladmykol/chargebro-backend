package com.vladmykol.takeandcharge.exceptions;

import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;

public class NoHandlerDefined extends ChargingStationException {
    public NoHandlerDefined(short command) {
        super("cannot map command " + MessageHeader.MessageCommand.byCommand(command) + " to any handler");
    }
}
