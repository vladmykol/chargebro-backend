package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationRegister {

    private final List<StationSocketClient> allConnectedStations = Collections.synchronizedList(new LinkedList<>());
    private final Map<String, StationSocketClientWrapper> authenticatedStations = new HashMap<>();

    public List<ClientInfo> getAllConnectedStations() {
        List<ClientInfo> result = new ArrayList<>();
        allConnectedStations.forEach(stationSocketClient -> {
            result.add(stationSocketClient.getClientInfo());
        });
        return result;
    }

    public void authStation(StationSocketClient stationSocketClient) {
        Assert.notNull(stationSocketClient.getClientInfo().getCabinetId(), "Station ID must not be null during authentication");

        synchronized (authenticatedStations) {
            final var stationSocketClientWrapper = authenticatedStations.get(stationSocketClient.getClientInfo().getCabinetId());
            if (stationSocketClientWrapper != null) {
                synchronized (stationSocketClientWrapper) {
                    removeConnectedStation(stationSocketClientWrapper.getSocketClient());
                    stationSocketClientWrapper.getSocketClient().shutdown(new RuntimeException("New client with same ID is connected"));
                    stationSocketClientWrapper.setSocketClient(stationSocketClient);
                    stationSocketClientWrapper.setLastLogin(new Date());
                    stationSocketClientWrapper.notifyAll();
                }
            } else {
                authenticatedStations.put(stationSocketClient.getClientInfo().getCabinetId(), new StationSocketClientWrapper(stationSocketClient));
            }
        }
    }

    public StationSocketClient getStation(String clientId) {
        var stationSocketClientWrapper = authenticatedStations.get(clientId);

        if (stationSocketClientWrapper == null) {
            throw new CabinetIsOffline();
        }

        if (stationSocketClientWrapper.getSocketClient().isActive()) {
            return stationSocketClientWrapper.getSocketClient();
        } else {
            synchronized (stationSocketClientWrapper) {
                final var waitTimeSec = 60;
                try {
                    log.debug("Station ID {} is offline so trying to wait {} sec for reconnection", clientId, waitTimeSec);
                    stationSocketClientWrapper.wait(waitTimeSec * 1000);
//                    sleep before working with station after connection. Station needs some time to check available powerbanks
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return stationSocketClientWrapper.getSocketClient();
    }

    public void addConnectedStation(StationSocketClient stationSocketClient) {
        allConnectedStations.add(stationSocketClient);
    }

    public void removeConnectedStation(StationSocketClient stationSocketClient) {
        allConnectedStations.remove(stationSocketClient);
    }

    public List<StationSocketClient> getInactiveClients(long seconds) {
        return allConnectedStations.stream()
                .filter(client -> client.getClientInfo().getLastSeen().isBefore(Instant.now().minusSeconds(seconds)))
                .collect(Collectors.toList());
    }

    @Data
    static class StationSocketClientWrapper {
        private StationSocketClient socketClient;
        private Date lastLogin;

        public StationSocketClientWrapper(StationSocketClient socketClient) {
            this.socketClient = socketClient;
            this.lastLogin = new Date();
        }
    }

}
