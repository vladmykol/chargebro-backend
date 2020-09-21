package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(EndpointConst.API_ADMIN)
@RequiredArgsConstructor
public class AdminController {
    private final RegisterUserService registerUserService;
    private final PaymentService paymentService;
    private final RentService rentService;
    private final StationService stationService;
    private final WebSocketServer webSocketServer;

    @PostMapping(API_ADMIN_USER)
    public void saveUser(@Valid @RequestBody User user) {
        registerUserService.saveUser(user);
    }

    @DeleteMapping(API_ADMIN_USER)
    public void deleteUser(@RequestParam String id) {
        registerUserService.deleteUser(id);
    }

    @GetMapping(API_ADMIN_USER)
    public List<User> findAll() {
        return registerUserService.findAll();
    }

    @GetMapping(API_ADMIN_PAYMENT)
    public List<Payment> getAllPaymentHistory() {
        return paymentService.getAllPaymentHistory();
    }

    @GetMapping(API_ADMIN_RENT_REPORT)
    public List<RentReportDto> getRentReport() {
        return rentService.getRentReport();
    }

    @GetMapping(API_STATIONS)
    public List<ClientInfo> getAllConnectedStations() {
        return stationService.getConnectedStations();
    }

    @GetMapping(API_ADMIN_ONLINE_CLIENT)
    public Map<String, List<String>> findAllConnectedWebSocketClients() {
        return webSocketServer.getConnectedClients();
    }

    @PostMapping(API_ADMIN_STATION_OPTIONS)
    public MessageHeader setStationOptions(@PathVariable(name = "id") String stationId,
                                           @RequestParam String serverAddress,
                                           @RequestParam(defaultValue = "10382") String serverPort,
                                           @RequestParam(defaultValue = "30") short interval
    ) {
        return stationService.setServerAddressAndRestart(stationId, serverAddress, serverPort, interval);
    }

}
