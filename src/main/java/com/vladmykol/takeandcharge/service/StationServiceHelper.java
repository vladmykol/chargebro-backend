package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.entity.Station;
import com.vladmykol.takeandcharge.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationServiceHelper {
    private final StationRepository stationRepository;

    public Station getByIdOrNew(String id) {
        final var optionalStation = stationRepository.findById(id);
        return optionalStation.orElseGet(Station::new);
    }

    public Station getByShortIdThrow(String shortId) {
        final var optionalStation = stationRepository.findByShortId(shortId);
        return optionalStation.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station with id " + shortId + " is not found"));
    }
}
