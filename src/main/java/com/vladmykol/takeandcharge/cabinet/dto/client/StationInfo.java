package com.vladmykol.takeandcharge.cabinet.dto.client;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.STRING;

@Data
public class StationInfo {
    @ProtocolField(position = 1, dataType = STRING)
    private String value;
}
