package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StationSocketServerTest {

    @Test
    void connectToRemote() throws IOException {
        var connection = new Cabinet("REDACTED_SERVER_IP", 10382);
        byte[] str = {0x00, 0x07, 0x61, 0x01, 0x00, 0x11, 0x22, 0x33, 0x44};

        var response = connection.communicate(str);

        System.out.println(response);
        System.out.println(HexDecimalConverter.toHexString(response));
    }


}
