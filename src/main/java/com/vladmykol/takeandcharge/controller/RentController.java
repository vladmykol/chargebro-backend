package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.exceptions.NoPowerBanksLeft;
import com.vladmykol.takeandcharge.service.RentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_RENT;

@RestController
@RequestMapping(API_RENT)
@RequiredArgsConstructor
public class RentController {
    private final RentService rentService;

    @PostMapping
    public String rentRequest(@RequestParam String stationId, HttpServletResponse response) throws IOException {
        String powerBankId = null;
        try {
            powerBankId = rentService.rent(stationId);
        } catch (NoPowerBanksLeft noPowerBanksLeft) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "No available power banks");
//            throw new ResponseStatusException(
//                    HttpStatus.PRECONDITION_FAILED, "No available power banks");
        }
        return powerBankId;
    }

    @GetMapping("/history")
    public List<RentHistoryDto> getRentHistory(@RequestParam(required = false) String filter, Principal principal, HttpServletResponse response) throws IOException {
        List<RentHistoryDto> rentHistory = rentService.getRentHistory(principal.getName(), "current".equals(filter));
        if (rentHistory.isEmpty())
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "not rent history");
        return rentHistory;
    }

}
