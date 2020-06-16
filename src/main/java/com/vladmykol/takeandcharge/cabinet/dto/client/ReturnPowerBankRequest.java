package com.vladmykol.takeandcharge.cabinet.dto.client;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.*;

@Data
public class ReturnPowerBankRequest {

    @ProtocolField(position = 1, dataType = BYTE)
    private short slotNumber;

    @ProtocolField(position = 2, dataType = BYTE8STRING)
    private String powerBankId;

}
