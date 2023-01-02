package com.vladmykol.takeandcharge.controller.admin;

import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.dto.AuthenticatedStationsDto;
import com.vladmykol.takeandcharge.dto.StationInfoDto;
import com.vladmykol.takeandcharge.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_ADMIN;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_STATIONS;

@RestController
@RequestMapping(API_ADMIN + API_STATIONS)
@RequiredArgsConstructor
public class AdminStationController {
    private final StationService stationService;

    @GetMapping("/connection")
    public List<AuthenticatedStationsDto> getAuthenticatedStations() {
        return stationService.getAuthenticatedStations();
    }

    @GetMapping()
    public List<StationInfoDto> getStations() {
        return stationService.findAll();
    }

    @DeleteMapping()
    public void deleteStation(@RequestParam String stationId) {
        stationService.deleteById(stationId);
    }

    @PostMapping()
    public void updateStationsInfo(@RequestBody StationInfoDto stationInfoDto) {
        stationService.update(stationInfoDto);
    }

    @PostMapping("/{shortId}/option")
    @Operation(description = "Set parameters for charging station if it is connected and restart. localhost - is a debug server")
    public MessageHeader setStationOptions(@PathVariable(name = "shortId") String shortId,
                                           @Parameter(required = true) @RequestParam(defaultValue = "localhost") String serverAddress,
                                           @RequestParam(defaultValue = "10382") String serverPort,
                                           @RequestParam(defaultValue = "30") short interval
    ) {
        return stationService.setServerAddressAndRestart(shortId, serverAddress, serverPort, interval);
    }

    @PostMapping("/{shortId}/reboot")
    @Operation(description = "Restart selected station")
    public MessageHeader setStationOptions(@PathVariable(name = "shortId") String shortId) {
        return stationService.restart(shortId);
    }

    @PostMapping("/{shortId}/unlock-all")
    @Operation(description = "unlock all power-bank for maintenance")
    public void unlockAll(@PathVariable(name = "shortId") String shortId) {
        stationService.unlockAllPowerBanks(shortId);
    }

}
