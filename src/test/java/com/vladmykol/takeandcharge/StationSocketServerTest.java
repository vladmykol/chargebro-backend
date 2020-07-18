package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.cabinet.StationSocketServer;
import org.apache.tomcat.util.buf.HexUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class StationSocketServerTest {
    @Value("${takeandcharge.socket.server.port}")
    private int portNumber;

    @Autowired
    private StationSocketServer stationSocketServer;

    @Autowired
    private ThreadPoolTaskExecutor clientTaskExecutor;

    @BeforeEach
    public void startServer() throws IOException {
//        socketServer.start(PORT_NUMBER);
//        Cabinet cabinet = new Cabinet("localhost", PORT_NUMBER);
//        Cabinet cabinet1 = new Cabinet("localhost", PORT_NUMBER);
    }

    @Test
    void shouldHandleCoupleClients() throws IOException {
        ArrayList<Cabinet> cabinets = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            cabinets.add(new Cabinet("localhost", portNumber));
        }
        cabinets.get(0).disconnect();
        cabinets.add(new Cabinet("localhost", portNumber));

        System.out.println(clientTaskExecutor.getActiveCount());

        assertThat(cabinets, hasSize(5));

    }

    @Test
    void printText(){
        byte[] str = {0x53,0x54,0x57,0x41,0x02,0x01,0x00,0x50};
        System.out.println(HexUtils.toHexString(str));
    }


}
