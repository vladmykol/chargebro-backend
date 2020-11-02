package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.StationInfoDto;
import com.vladmykol.takeandcharge.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(API_STATIONS)
@RequiredArgsConstructor
public class StationInfoController {
    private final StationService stationService;

    @GetMapping(API_STATIONS_NEARBY)
    public List<StationInfoDto> getStations(@RequestParam double x,
                                            @RequestParam double y) {
        final var stationsNearBy = stationService.findStationsNearBy(x, y);
        if (stationsNearBy == null || stationsNearBy.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no stations in this area");
        }
        return stationsNearBy;
    }

    @GetMapping(API_STATIONS_CAPACITY)
    public int getRemainingPowerBanks(@PathVariable String id) {
        return stationService.getRemainingPowerBanks(stationService.extractStationId(id));
    }

}
