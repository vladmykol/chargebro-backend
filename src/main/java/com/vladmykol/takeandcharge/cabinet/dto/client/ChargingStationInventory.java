package com.vladmykol.takeandcharge.cabinet.dto.client;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import java.util.ArrayList;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;

@Data
public class ChargingStationInventory {

    @ProtocolField(position = 1, dataType = BYTE)
    private short remainingPowerBanks;

    private ArrayList<PowerBankInfo> powerBankList = new ArrayList<>();

}
