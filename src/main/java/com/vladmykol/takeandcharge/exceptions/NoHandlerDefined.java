package com.vladmykol.takeandcharge.exceptions;

import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;

public class NoHandlerDefined extends ChargingStationException {
    public NoHandlerDefined(short command) {
        super(MessageHeader.MessageCommand.stringValueByCommand(command));
    }
}
