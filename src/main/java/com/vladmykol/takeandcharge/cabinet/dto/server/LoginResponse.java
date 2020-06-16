package com.vladmykol.takeandcharge.cabinet.dto.server;

import com.vladmykol.takeandcharge.cabinet.dto.CommonResult;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;

@Data
public class LoginResponse {

    @ProtocolField(position = 1, dataType = BYTE)
    private final short result;

    public LoginResponse(CommonResult resultEnum) {
        this.result = resultEnum.getValue();
    }

}
