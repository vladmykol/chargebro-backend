package com.vladmykol.takeandcharge.cabinet.dto.server;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;

@Data
@RequiredArgsConstructor
public class TakePowerBankRequest {
    @ProtocolField(position = 1, dataType = BYTE)
    private final short slotNumber;
}
