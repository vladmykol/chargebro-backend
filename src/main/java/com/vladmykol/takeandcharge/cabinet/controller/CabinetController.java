package com.vladmykol.takeandcharge.cabinet.controller;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.client.LoginRequest;
import com.vladmykol.takeandcharge.cabinet.dto.client.ReturnPowerBankRequest;
import com.vladmykol.takeandcharge.cabinet.dto.server.LoginResponse;
import com.vladmykol.takeandcharge.cabinet.dto.server.ReturnPowerBankResponse;
import com.vladmykol.takeandcharge.entity.RentHistory;
import com.vladmykol.takeandcharge.repository.RentHistoryRepository;
import com.vladmykol.takeandcharge.service.RentService;
import com.vladmykol.takeandcharge.service.RentWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Optional;

import static com.vladmykol.takeandcharge.cabinet.dto.CommonResult.OK;

@Slf4j
@Component
@RequiredArgsConstructor
public class CabinetController {
    private final RentService rentService;

    public  ProtocolEntity<Object> heartBeat(ProtocolEntity<RawMessage> request) {
        return new ProtocolEntity<>(request.getHeader(), null);
    }

    public  ProtocolEntity<LoginResponse> singIn(ProtocolEntity<RawMessage> request, ClientInfo clientInfo) {
        LoginRequest loginRequest = request.getBody().readFullyTo(new LoginRequest());
        log.debug("Sing in request {} and {}", request.getHeader(), loginRequest);
        request.getHeader().setCheckSum((short) 7);
        request.getHeader().setTime(Instant.now());

        clientInfo.setCabinetId(loginRequest.getBoxId());

        LoginResponse loginResponse = new LoginResponse(OK);

        return new ProtocolEntity<>(request.getHeader(), loginResponse);
    }

    public ProtocolEntity<ReturnPowerBankResponse> returnPowerBank(ProtocolEntity<RawMessage> request) {
        ReturnPowerBankRequest requestBody = request.getBody().readFullyTo(new ReturnPowerBankRequest());
        log.debug("Power Bank return request {} and {}", request.getHeader(), requestBody);

        ReturnPowerBankResponse returnPowerBankResponse = ReturnPowerBankResponse.builder()
                .slotNumber(requestBody.getSlotNumber())
                .result(OK)
                .build();

        rentService.returnRent(requestBody.getPowerBankId());

        return new ProtocolEntity<>(request.getHeader(), returnPowerBankResponse);
    }
}
