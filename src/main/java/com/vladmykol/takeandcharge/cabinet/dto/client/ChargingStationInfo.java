package com.vladmykol.takeandcharge.cabinet.dto.client;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;

@Data
public class ChargingStationInfo {

    @ProtocolField(position = 1, dataType = BYTE)
    private short remainingPowerBanks;

}
