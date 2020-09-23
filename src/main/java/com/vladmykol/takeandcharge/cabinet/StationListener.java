package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.config.AsyncConfiguration;
import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class StationListener {
    private final LinkedList<StationSocketClient> registeredStationSocketClients = new LinkedList<>();

    public List<ClientInfo> listConnectedClients() {
        List<ClientInfo> result = new ArrayList<>();
        registeredStationSocketClients.forEach(stationSocketClient -> {
            result.add(stationSocketClient.getClientInfo());
        });
        return result;
    }

    public StationSocketClient getClient(String clientId) {
        Optional<StationSocketClient> optionalClient = registeredStationSocketClients.stream().filter(client -> clientId.equals(client.getClientInfo().getCabinetId())).findFirst();
        if (optionalClient.isEmpty()) {
            synchronized (registeredStationSocketClients) {
                final var waitTimeSec = 60;
                try {
                    log.debug("Station ID {} is offline so trying to wait for next connected for {} sec", clientId, waitTimeSec);
                    registeredStationSocketClients.wait(waitTimeSec * 1000);
                    // TODO: 9/23/2020 need smth else to wait before stations is authenticated after connection
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            optionalClient = registeredStationSocketClients.stream().filter(client -> clientId.equals(client.getClientInfo().getCabinetId())).findFirst();
        }

        if (optionalClient.isEmpty()) {
            throw new CabinetIsOffline();
        }

        return optionalClient.get();
    }

    public void removeClient(StationSocketClient stationSocketClient) {
        synchronized (registeredStationSocketClients) {
            registeredStationSocketClients.removeLastOccurrence(stationSocketClient);
        }
    }

    public void registerClient(StationSocketClient stationSocketClient) {
        synchronized (registeredStationSocketClients) {
            registeredStationSocketClients.addFirst(stationSocketClient);
            registeredStationSocketClients.notify();
        }
    }

    public List<StationSocketClient> getInactiveClients(long seconds) {
        return registeredStationSocketClients.stream()
                .filter(client -> client.getClientInfo().getLastSeen().isBefore(Instant.now().minusSeconds(seconds)))
                .collect(Collectors.toList());
    }

    @Async(AsyncConfiguration.ONLINE_WEB_SOCKET_CLIENTS_TASK_EXECUTOR)
    public void listen(StationSocketClient stationSocketClient) {
        try {
            newStationSocketHandler(stationSocketClient).handle();
        } catch (Exception e) {
            if (stationSocketClient.isActive()) {
                removeClient(stationSocketClient);
                stationSocketClient.shutdown(e);
            }
        }
    }

    @Lookup
    protected abstract StationSocketHandler newStationSocketHandler(StationSocketClient stationSocketClient);

}

