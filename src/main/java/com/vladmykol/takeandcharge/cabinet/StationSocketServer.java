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
    @Value("${takeandcharge.socket.server.port}")
    private int portNumber;
    @Value("${takeandcharge.socket.server.client.idle-timeout-sec}")
    private int idleTimeoutSeconds;

    @Async(AsyncConfiguration.serverTaskExecutorName)
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            log.info("Socket server started on port: {}", portNumber);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                StationSocketClient stationSocketClient = new StationSocketClient(socket);
                stationListener.listen(stationSocketClient);
                stationListener.registerClient(stationSocketClient);
            }
        }
    }

    @Scheduled(fixedRate = 10000, initialDelay = 30000)
    public void monitorClients() {
        List<StationSocketClient> inactiveStationSocketClients = stationListener.getInactiveClients(idleTimeoutSeconds);
        inactiveStationSocketClients.forEach(this::tryToWakeUpInactive);
    }

    public void tryToWakeUpInactive(StationSocketClient stationSocketClient) {
        log.debug("Try to wake up inactive client {}", stationSocketClient.getClientInfo().getIpAddress());
        try {
            stationListener.removeClient(stationSocketClient);
            stationSocketClient.ping();
            stationSocketClient.check();
            stationListener.registerClient(stationSocketClient);
        } catch (Exception e) {
            stationListener.removeClient(stationSocketClient);
            if (stationSocketClient.isActive()) {
                stationSocketClient.shutdown(e);
            }
        }
    }

}
