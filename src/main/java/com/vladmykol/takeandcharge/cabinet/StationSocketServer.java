package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.config.AsyncConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StationSocketServer {
    private final StationListener stationListener;
    private final StationRegister stationRegister;
    @Value("${takeandcharge.socket.server.port}")
    private int portNumber;
    @Value("${takeandcharge.socket.server.client.idle-timeout-sec}")
    private int idleTimeoutSeconds;

    @Async(AsyncConfiguration.STATION_SERVER_TASK_EXECUTOR)
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            log.info("Socket server started on port: {}", portNumber);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                StationSocketClient stationSocketClient = new StationSocketClient(socket, idleTimeoutSeconds);
                stationListener.listen(stationSocketClient);
                stationRegister.addConnectedStation(stationSocketClient);
            }
        }
    }

    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    public void monitorClients() {
        List<StationSocketClient> inactiveStationSocketClients = stationRegister.getInactiveClients(idleTimeoutSeconds);
        inactiveStationSocketClients.forEach(this::tryToWakeUpInactive);
    }

//    @Scheduled(fixedRate = 15000, initialDelay = 20000)
//    public void checkClients() {
//        List<StationSocketClient> activeClients = stationRegister.getInactiveClients(idleTimeoutSeconds / 2);
//        activeClients.forEach(this::checkActiveClient);
//    }

    public void tryToWakeUpInactive(StationSocketClient stationSocketClient) {
        stationSocketClient.setInactive();
        try {
            stationSocketClient.check();
            stationSocketClient.setActive();
        } catch (Exception e) {
            stationSocketClient.shutdown(e);
        }
    }

}
