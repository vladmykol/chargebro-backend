package com.vladmykol.takeandcharge.controller.admin;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.dto.AuthenticatedStationsDto;
import com.vladmykol.takeandcharge.dto.StationInfoDto;
import com.vladmykol.takeandcharge.service.StationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @GetMapping("/connected")
    public List<ClientInfo> getAllConnectedStations() {
        return stationService.getAllConnectedStations();
    }

    @GetMapping("/authenticated")
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

    @PostMapping("/{id}/option")
    @ApiOperation(value = "Set parameters for charging station if it is connected and restart. localhost - is a debug server")
    public MessageHeader setStationOptions(@ApiParam(allowableValues = "STWA062001000013, STWA312001000005") @PathVariable(name = "id") String stationId,
                                           @ApiParam(allowableValues = "localhost, REDACTED_SERVER_IP", required = true)
                                           @RequestParam(defaultValue = "localhost") String serverAddress,
                                           @RequestParam(defaultValue = "10382") String serverPort,
                                           @RequestParam(defaultValue = "30") short interval
    ) {
        return stationService.setServerAddressAndRestart(stationId, serverAddress, serverPort, interval);
    }

}
