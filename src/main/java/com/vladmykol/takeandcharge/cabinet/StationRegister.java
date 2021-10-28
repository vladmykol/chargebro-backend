package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.dto.AuthenticatedStationsDto;
import com.vladmykol.takeandcharge.dto.DisconnectedStationsDto;
import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import com.vladmykol.takeandcharge.exceptions.NoResponseFromWithinTimeout;
import com.vladmykol.takeandcharge.monitoring.TelegramNotifierService;
import com.vladmykol.takeandcharge.service.StationService;
import com.vladmykol.takeandcharge.service.WebSocketServer;
import com.vladmykol.takeandcharge.utils.TimeUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationRegister {

    private final List<StationSocketClient> currentConnections = Collections.synchronizedList(new LinkedList<>());
    private final Map<String, StationSocketClientWrapper> connections = Collections.synchronizedMap(new HashMap<>());
    private final WebSocketServer webSocketServer;
    private final TelegramNotifierService telegramNotifierService;

    public List<DisconnectedStationsDto> getDisconnectedStations() {
        List<DisconnectedStationsDto> disconnectedStations = new ArrayList<>();
        connections.forEach((stationId, stationSocketClientWrapper) -> {
            if (!stationSocketClientWrapper.getSocketClient().isActive()
                    && stationSocketClientWrapper.getSocketClient().getClientInfo().getLastSeen().isBefore(Instant.now().minusSeconds(5 * 60))
                    && !stationSocketClientWrapper.isReportedInactive) {
                stationSocketClientWrapper.setReportedInactive(true);
                var timeSince = TimeUtils.timeBetweenWords(stationSocketClientWrapper.getSocketClient().getClientInfo().getLastSeen());
                disconnectedStations.add(new DisconnectedStationsDto(stationId, timeSince));
            }
        });

        return disconnectedStations;
    }

    public List<AuthenticatedStationsDto> getConnections() {
        List<AuthenticatedStationsDto> result = new ArrayList<>();
        connections.forEach((stationId, stationSocketClientWrapper) -> {
            final var dto = AuthenticatedStationsDto.builder()
                    .stationId(stationId)
                    .isOnline(stationSocketClientWrapper.getSocketClient().isActive())
                    .lastSeen(stationSocketClientWrapper.getSocketClient().getClientInfo().getLastSeen())
                    .pastSessions(new ArrayList<>(stationSocketClientWrapper.getLastSessions()))
                    .timeSinceLastLogIn(TimeUtils.timeSince(stationSocketClientWrapper.getLogInTime()))
                    .build();

            result.add(dto);
        });
        return result;
    }

    public void authStation(StationSocketClient newStationSocketClient) {
        Assert.notNull(newStationSocketClient.getClientInfo().getCabinetId(), "Station ID must not be null during authentication");

        final var stationSocketClientWrapper = connections.get(newStationSocketClient.getClientInfo().getCabinetId());
        if (stationSocketClientWrapper != null) {
            synchronized (stationSocketClientWrapper) {
//                    removeConnectedStation(stationSocketClientWrapper.getSocketClient());
//                    if (stationSocketClientWrapper.getSocketClient().isSocketConnected()) {
                stationSocketClientWrapper.getSocketClient().shutdown(new RuntimeException("Connection for station " +
                        stationSocketClientWrapper.getSocketClient().getClientInfo() + " is replaces by " + newStationSocketClient.getClientInfo()));
//                    }
                // set session duration before reconnect
                final var sessionDuration = TimeUtils.timeBetween(stationSocketClientWrapper.getLogInTime(),
                        stationSocketClientWrapper.getSocketClient().getClientInfo().getShutdownTime());
                stationSocketClientWrapper.getLastSessions().add(sessionDuration);

                stationSocketClientWrapper.setSocketClient(newStationSocketClient);
                stationSocketClientWrapper.setLogInTime(Instant.now());
                if (stationSocketClientWrapper.isReportedInactive) {
                    telegramNotifierService.backOnline(newStationSocketClient.getClientInfo().getCabinetId());
                }

                stationSocketClientWrapper.setReportedInactive(false);

                stationSocketClientWrapper.notify();
            }
        } else {
            var stationId = newStationSocketClient.getClientInfo().getCabinetId();
            connections.put(stationId, new StationRegister.StationSocketClientWrapper(newStationSocketClient));
            telegramNotifierService.stationConnected(stationId);
        }
    }

    private synchronized StationSocketClient getStation(String clientId) {
        var stationSocketClientWrapper = connections.get(clientId);

        if (stationSocketClientWrapper == null) {
            throw new CabinetIsOffline();
        }

        if (stationSocketClientWrapper.getSocketClient().isActive()) {
            return stationSocketClientWrapper.getSocketClient();
        } else {

            final var lastSeenMinBefore = stationSocketClientWrapper.getSocketClient().getClientInfo().getLastSeen().until(Instant.now(), ChronoUnit.MINUTES);
            if (lastSeenMinBefore > 3) {
                throw new CabinetIsOffline();
            } else {
                synchronized (stationSocketClientWrapper) {
                    final var waitTimeSec = 20;
                    try {
                        log.info("Station {} is offline so trying to wait {} sec for reconnection",
                                stationSocketClientWrapper.getSocketClient().getClientInfo(), waitTimeSec);
                        webSocketServer.sendResolveConnectionIssue();
                        stationSocketClientWrapper.wait(waitTimeSec * 1000);
//                    sleep before working with station after connection. Station needs some time to check available powerbanks
                        Thread.sleep(7000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return stationSocketClientWrapper.getSocketClient();
    }

    public ProtocolEntity<RawMessage> communicateWithStation(String cabinetId, ProtocolEntity<?> stockRequest) {
        StationSocketClient stationSocketClient;
        ProtocolEntity<RawMessage> messageFromClient;
        try {
            stationSocketClient = getStation(cabinetId);
            messageFromClient = stationSocketClient.communicate(stockRequest);
        } catch (NoResponseFromWithinTimeout e) {
            stationSocketClient = getStation(cabinetId);
            messageFromClient = stationSocketClient.communicate(stockRequest);
        }
        return messageFromClient;
    }

    public void addConnectedStation(StationSocketClient stationSocketClient) {
        currentConnections.add(stationSocketClient);
    }

    public void removeConnectedStation(StationSocketClient stationSocketClient) {
        currentConnections.remove(stationSocketClient);
    }

    public List<StationSocketClient> getInactiveClients(long seconds) {
        return currentConnections.stream()
                .filter(client -> client.getClientInfo().getLastSeen().isBefore(Instant.now().minusSeconds(seconds)) && client.isActive())
                .collect(Collectors.toList());
    }

    @Data
    static class StationSocketClientWrapper {
        private StationSocketClient socketClient;
        private Instant logInTime;
        private boolean isReportedInactive;
        private CircularFifoQueue<String> lastSessions = new CircularFifoQueue<>(10);

        public StationSocketClientWrapper(StationSocketClient socketClient) {
            this.socketClient = socketClient;
            this.logInTime = Instant.now();
        }
    }

}
