package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.controller.CabinetController;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.serialization.CustomDataInputStream;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.exceptions.NoHandlerDefined;
import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StationSocketHandler {
    private final DataInputStream in;
    private final StationSocketClient stationSocketClient;
    private CabinetController cabinetController;

    //    used by @lookup
    public StationSocketHandler(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") StationSocketClient stationSocketClient) throws IOException {
        this.stationSocketClient = stationSocketClient;
        this.in = stationSocketClient.getInputStream();
    }

    @Autowired
    public void setCabinetController(CabinetController cabinetController) {
        this.cabinetController = cabinetController;
    }

    public void handle() throws IOException {
        while (!Thread.currentThread().isInterrupted() && stationSocketClient.isSocketConnected()) {
            try {
                ProtocolEntity<RawMessage> incomingMessage = readIncomingMessage();
                authenticate();
                if (!stationSocketClient.fulfillPendingRequest(incomingMessage)) {
                    stationSocketClient.writeMessage(dispatch(incomingMessage));
                }
            } catch (SocketTimeoutException ignore) {
            } catch (NoHandlerDefined e) {
                log.error("Not handled message for station client - {}", stationSocketClient.getClientInfo(), e);
            } catch (Exception e) {
                try {
                    in.close();
                } catch (Exception ignore) {
                }
                throw e;
            }
        }
    }

    private ProtocolEntity<?> dispatch(ProtocolEntity<RawMessage> incomingMessage) {
        ProtocolEntity<?> value;
        switch (incomingMessage.getCommand()) {
            case 0x60:
                value = cabinetController.singIn(incomingMessage, stationSocketClient);
                break;
            case 0x61:
                value = cabinetController.heartBeat(incomingMessage);
                break;
            case 0x66:
                value = cabinetController.returnPowerBank(incomingMessage, stationSocketClient.getClientInfo().getCabinetId());
                break;
            default:
                throw new NoHandlerDefined(incomingMessage.getCommand());
        }
        return value;
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

    private synchronized byte[] readInputStream() throws IOException {
        int messageLength = in.readUnsignedShort();
        log.trace("Reading message from station. Message length {}", messageLength);
        byte[] bytes = in.readNBytes(messageLength);
        log.trace("Reading message from station. Message content {}", HexDecimalConverter.toHexString(bytes));
        return bytes;
    }
}