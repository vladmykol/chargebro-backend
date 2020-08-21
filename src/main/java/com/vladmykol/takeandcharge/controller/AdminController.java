package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.entity.LiqPayHistory;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.service.PaymentService;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import com.vladmykol.takeandcharge.service.RentWebSocket;
import com.vladmykol.takeandcharge.service.StationService;
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
    private final StationService stationService;
    private final RentWebSocket rentWebSocket;

    @PostMapping(API_ADMIN_USERS)
    public void saveUser(@Valid @RequestBody User user) {
        registerUserService.saveUser(user);
    }

    @DeleteMapping(API_ADMIN_USERS)
    public void deleteUser(@RequestParam String id) {
        registerUserService.deleteUser(id);
    }

    @GetMapping(API_ADMIN_USERS)
    public List<User> findAll() {
        return registerUserService.findAll();
    }

    @GetMapping(API_ADMIN_HISTORY)
    public List<LiqPayHistory> getAllPaymentHistory() {
        return paymentService.getAllPaymentHistory();
    }

    @GetMapping(API_STATIONS)
    public List<ClientInfo> getAllConnectedStations() {
        return stationService.getConnectedStations();
    }

    @GetMapping(API_ADMIN_SOCKET_CLIENTS)
    public Map<String, List<String>> findAllConnectedWebSocketClients() {
        return rentWebSocket.listConnectedClients();
    }

    @PostMapping(API_ADMIN_STATION_OPTIONS)
    public MessageHeader setStationOptions(@PathVariable(name = "id") String stationId,
                                           @RequestParam String serverAddress,
                                           @RequestParam String serverPort,
                                            @RequestParam short pingSec
    ) {
        return stationService.setServerAddress(stationId, serverAddress, serverPort, pingSec);
    }

}
