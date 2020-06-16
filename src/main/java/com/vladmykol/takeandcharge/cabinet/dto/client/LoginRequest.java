package com.vladmykol.takeandcharge.cabinet.dto.client;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.*;

@Data
public class LoginRequest {

    @ProtocolField(position = 1, dataType = UINT32)
    private long randomNumber;

    @ProtocolField(position = 2, dataType = UINT16)
    private int magicWord;

    @ProtocolField(position = 3)
    private String boxId;

    @ProtocolField(position = 4)
    private String regData;

}
