package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.config.AsyncConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class StationListener {
    private final StationRegister stationRegister;

    @Async(AsyncConfiguration.STATION_LISTENER_TASK_EXECUTOR)
    public void listen(StationSocketClient stationSocketClient) {
        try {
            newStationSocketHandler(stationSocketClient).handle();
        } catch (Exception e) {
            log.debug("Disconnect station socket client {}", stationSocketClient.getClientInfo(), e);
            stationRegister.removeConnectedStation(stationSocketClient);
            stationSocketClient.shutdown(e);
        }
    }

    @Lookup
    protected abstract StationSocketHandler newStationSocketHandler(StationSocketClient stationSocketClient);

}

