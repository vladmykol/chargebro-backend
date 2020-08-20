package com.vladmykol.takeandcharge.cabinet.dto.server;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;

@Getter
@Builder
public class ChangeServerAddressRequest {

    @ProtocolField(position = 1)
    private String serverAddress;

    @ProtocolField(position = 2)
    private String serverPort;

    @ProtocolField(position = 3, dataType = BYTE)
    private short heartbeatIntervalSec;

}
