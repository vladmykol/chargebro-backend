package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.controller.StationController;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.serialization.CustomDataInputStream;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.exceptions.UnknownCommand;
import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StationSocketHandler {
    private final DataInputStream in;
    private final StationSocketClient stationSocketClient;
    private StationController stationController;

    //    used by @lookup
    public StationSocketHandler(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") StationSocketClient stationSocketClient) throws IOException {
        this.stationSocketClient = stationSocketClient;
        this.in = new DataInputStream(new BufferedInputStream(stationSocketClient.getInputStream()));
    }

    @Autowired
    public void setStationController(StationController stationController) {
        this.stationController = stationController;
    }

    public void handle() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            ProtocolEntity<RawMessage> incomingMessage = readIncomingMessage();
            authenticate();
            if (!stationSocketClient.fulfillPendingRequest(incomingMessage)) {
                stationSocketClient.writeMessage(
                        switch (incomingMessage.getCommand()) {
                            case 0x60 -> stationController.singIn(incomingMessage, stationSocketClient.getClientInfo());
                            case 0x61 -> stationController.heartBeat(incomingMessage);
                            case 0x66 -> stationController.returnPowerBank(incomingMessage);
                            default -> throw new UnknownCommand(
                                    incomingMessage.getCommand() + " cannot map message to any know handler");
                        });
            }
        }
    }

    // TODO: 6/5/2020 no authentication info in incoming message
    private void authenticate() {
        String cabinetId = stationSocketClient.getClientInfo().getCabinetId();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(cabinetId, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ProtocolEntity<RawMessage> readIncomingMessage() throws IOException {
        byte[] byteArray = readInputStream();
        stationSocketClient.getClientInfo().setLastSeen(Instant.now());

        CustomDataInputStream customDataInputStream = new CustomDataInputStream(new ByteArrayInputStream(byteArray));
        MessageHeader messageHeader = ProtocolSerializationUtils.readObject(customDataInputStream, new MessageHeader());
        return new ProtocolEntity<>(messageHeader, new RawMessage(customDataInputStream));
    }

    private byte[] readInputStream() throws IOException {
        int messageLength = in.readUnsignedShort();
        byte[] bytes = in.readNBytes(messageLength);
        log.debug("Reading message from client {}", HexDecimalConverter.toHexString(bytes));
        return bytes;
    }
}