package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.StationInfoDto;
import com.vladmykol.takeandcharge.service.RentService;
import com.vladmykol.takeandcharge.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(API_STATIONS)
@RequiredArgsConstructor
public class StationInfoController {
    private final StationService stationService;
    private final RentService rentService;

    @GetMapping()
    public List<StationInfoDto> getStations() {
        return stationService.findAll();
    }

    @GetMapping(API_STATIONS_NEARBY)
    public List<StationInfoDto> getStations(@RequestParam double x,
                                            @RequestParam double y) {
        return stationService.findStationsNearBy(x, y);
    }

    @GetMapping(API_STATIONS_CAPACITY)
    public int getRemainingPowerBanks(@PathVariable String id) {
        return rentService.getRemainingPowerBanks(id);
    }

    @PostMapping()
    public void updateStationsInfo(@RequestBody StationInfoDto stationInfoDto) {
        stationService.update(stationInfoDto);
    }


}
