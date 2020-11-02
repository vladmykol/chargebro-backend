package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.service.RentFlowService;
import com.vladmykol.takeandcharge.service.RentService;
import com.vladmykol.takeandcharge.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_RENT;

@RestController
@RequestMapping(API_RENT)
@RequiredArgsConstructor
public class RentController {
    private final RentFlowService rentFlowService;
    private final RentService rentService;
    private final StationService stationService;

    @GetMapping
    public RentConfirmationDto getInfoBeforeRent(@RequestParam String stationId) {
        return rentFlowService.getBeforeRentInfo(stationService.extractStationId(stationId));
    }

    @PostMapping
    public void rentRequest(@RequestParam String stationId) {
        rentFlowService.syncRentStart(stationService.extractStationId(stationId));
    }

    @GetMapping("/history")
    public List<RentHistoryDto> getRentHistory(@RequestParam(required = false) Boolean onlyActive) {
        List<RentHistoryDto> rentHistory = rentService.getRentHistory(onlyActive);
        if (rentHistory.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no rent history");
        return rentHistory;
    }

}
