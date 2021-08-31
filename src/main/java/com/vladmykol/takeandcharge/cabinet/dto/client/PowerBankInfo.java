package com.vladmykol.takeandcharge.cabinet.dto.client;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import java.util.Date;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.*;

@Data
public class PowerBankInfo {

    @ProtocolField(position = 1, dataType = BYTE)
    private short slotNumber;

    @ProtocolField(position = 2, dataType = BYTE8STRING)
    private String powerBankId;

    @ProtocolField(position = 3, dataType = BYTE)
    private short powerLevel;

    private Date lastTakeAt;
}
