package com.vladmykol.takeandcharge.cabinet.dto.server;

import com.vladmykol.takeandcharge.cabinet.dto.CommonResult;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Builder;
import lombok.Data;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;

@Data
@Builder
public class ReturnPowerBankResponse {

    @ProtocolField(position = 1, dataType = BYTE)
    private short slotNumber;

    @ProtocolField(position = 2, dataType = BYTE)
    private short result;

    public static class ReturnPowerBankResponseBuilder {
        public ReturnPowerBankResponseBuilder result(CommonResult result) {
            this.result = result.getValue();
            return this;
        }
    }
}
