package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.service.RentService;
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
    private final RentService rentService;

    @GetMapping
    public RentConfirmationDto getInfoBeforeRent(@RequestParam String stationId) {
        return rentService.getBeforeRentInfo(stationId);
    }

    @PostMapping
    public void rentRequest(@RequestParam String stationId) {
        rentService.prepareForRentStart(stationId);
    }

    @GetMapping("/history")
    public List<RentHistoryDto> getRentHistory(@RequestParam(required = false) Boolean onlyActive) {
        List<RentHistoryDto> rentHistory = rentService.getRentHistory(onlyActive);
        if (rentHistory.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no rent history");
        return rentHistory;
    }

}
