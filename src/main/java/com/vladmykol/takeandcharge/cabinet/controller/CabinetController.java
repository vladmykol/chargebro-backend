package com.vladmykol.takeandcharge.cabinet.controller;

import com.vladmykol.takeandcharge.cabinet.StationSocketClient;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.dto.client.LoginRequest;
import com.vladmykol.takeandcharge.cabinet.dto.client.ReturnPowerBankRequest;
import com.vladmykol.takeandcharge.cabinet.dto.server.LoginResponse;
import com.vladmykol.takeandcharge.cabinet.dto.server.ReturnPowerBankResponse;
import com.vladmykol.takeandcharge.service.PowerBankService;
import com.vladmykol.takeandcharge.service.RentFlowService;
import com.vladmykol.takeandcharge.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.vladmykol.takeandcharge.cabinet.dto.CommonResult.ERROR;
import static com.vladmykol.takeandcharge.cabinet.dto.CommonResult.OK;

@Slf4j
@Component
@RequiredArgsConstructor
public class CabinetController {
    private final RentFlowService rentFlowService;
    private final StationService stationService;
    private final PowerBankService powerBankService;

    public ProtocolEntity<Object> heartBeat(ProtocolEntity<RawMessage> request) {
        return new ProtocolEntity<>(request.getHeader(), null);
    }

    public ProtocolEntity<LoginResponse> singIn(ProtocolEntity<RawMessage> request, StationSocketClient stationSocketClient) {
        LoginRequest loginRequest = request.getBody().readFullyTo(new LoginRequest());
        log.debug("Sing in request {} and {}", request.getHeader(), loginRequest);
        request.getHeader().setCheckSum((short) 7);
        request.getHeader().setTime(Instant.now());

        LoginResponse loginResponse;

        if (stationService.singIn(loginRequest, stationSocketClient)) {
            loginResponse = new LoginResponse(OK);
        } else {
            loginResponse = new LoginResponse(ERROR);
        }

        return new ProtocolEntity<>(request.getHeader(), loginResponse);
    }

    public ProtocolEntity<ReturnPowerBankResponse> returnPowerBank(ProtocolEntity<RawMessage> request, String cabinetId) {
        ReturnPowerBankRequest requestBody = request.getBody().readFullyTo(new ReturnPowerBankRequest());
        log.debug("Power Bank return request {} and {}", request.getHeader(), requestBody);

        ReturnPowerBankResponse returnPowerBankResponse = ReturnPowerBankResponse.builder()
                .slotNumber(requestBody.getSlotNumber())
                .result(OK)
                .build();

        final var rentId = powerBankService.returnAction(requestBody);
        if (rentId != null) {
            rentFlowService.returnPowerBankAction(rentId, cabinetId);
        }

        return new ProtocolEntity<>(request.getHeader(), returnPowerBankResponse);
    }
}
