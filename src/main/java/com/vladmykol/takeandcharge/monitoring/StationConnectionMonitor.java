package com.vladmykol.takeandcharge.monitoring;

import com.vladmykol.takeandcharge.cabinet.StationRegister;
import com.vladmykol.takeandcharge.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_MINUTE;

@Service
@RequiredArgsConstructor
public class StationConnectionMonitor {
    private final StationRegister stationRegister;
    private final TelegramNotifierService telegramNotifierService;
    private final StationRepository stationRepository;

    @Scheduled(fixedRate = 5 * MILLIS_PER_MINUTE, initialDelay = 3 * MILLIS_PER_MINUTE)
    public void monitorClients() {
        final var disconnectedStations = stationRegister.getDisconnectedStations();
        if (!disconnectedStations.isEmpty()) {
            final var stationEntityList = stationRepository.findAllById(disconnectedStations);
            stationEntityList.forEach(station -> {
                String msg = "Station " + station.getId() + " went offline" +
                        "\n\nPlace: " + station.getPlaceName() +
                        "\nPhone:" + station.getSimPhoneNumber();
                telegramNotifierService.messageToAdmin(msg);
            });
        }
    }

}
