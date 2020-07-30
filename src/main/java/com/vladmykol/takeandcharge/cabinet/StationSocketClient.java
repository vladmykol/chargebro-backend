package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.HEART_BEAT;
import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.SOFTWARE_VERSION;


@Slf4j
@EqualsAndHashCode(exclude = {"pendingResponse"})
public class StationSocketClient {
    @Getter
    private final ClientInfo clientInfo;
    private final Socket socket;
    @Getter
    private final Map<Short, ProtocolMessage> pendingResponse = Collections.synchronizedMap(new HashMap<>());
    private final OutputStream out;
    @Getter
    private volatile boolean isActive = true;

    public StationSocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.clientInfo = new ClientInfo(socket.getInetAddress());
        this.out = socket.getOutputStream();
        log.debug("Client {} is now connected", clientInfo.getIpAddress());
    }

    public static void putUnsignedShort(ByteBuffer bb, int value) {
        bb.putShort((short) (value & 0xffff));
    }

    @SneakyThrows
    public void shutdown(Exception reason) {
        isActive = false;
        socket.close();
        log.debug("Client {} is now disconnected", clientInfo.getIpAddress(), reason);
    }

    public void ping() throws IOException {
        ProtocolEntity<?> softwareVersionRequest = new ProtocolEntity<>(HEART_BEAT);
        writeMessage(softwareVersionRequest);
    }

    public void check() {
        ProtocolEntity<?> softwareVersionRequest = new ProtocolEntity<>(SOFTWARE_VERSION);
        communicate(softwareVersionRequest, 33000);
    }

    @SneakyThrows
    public ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request) {
        return communicate(request, 60000);
    }

    @SneakyThrows
    public ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request, int timeout) {
        ProtocolEntity<RawMessage> incomingMessage = waitAndGetResponse(request, timeout);

        if (incomingMessage == null)
            if (isActive)
                throw new TimeoutException("no response from a client");
            else throw new RuntimeException("client is not active any more");
        else
            return incomingMessage;
    }

    public void writeMessage(ProtocolEntity<?> protocolEntity) throws IOException {
        byte[] byteArrayMessage;
        if (protocolEntity.hasBody()) {
            byteArrayMessage = ProtocolSerializationUtils.serialize(protocolEntity.getHeader(), protocolEntity.getBody());
        } else {
            byteArrayMessage = ProtocolSerializationUtils.serialize(protocolEntity.getHeader());
        }

        writeOutputStream(byteArrayMessage);
    }

    public boolean fulfillPendingRequest(ProtocolEntity<RawMessage> incomingMessage) {
        ProtocolMessage pendingRequest = pendingResponse.get(incomingMessage.getCommand());
        if (pendingRequest != null) {
            synchronized (pendingRequest) {
                pendingRequest.setResponse(incomingMessage);
                pendingRequest.notify();
            }
        }
        return pendingRequest != null;
    }

    private ProtocolEntity<RawMessage> waitAndGetResponse(ProtocolEntity<?> request, int timeout)
            throws InterruptedException, IOException {
        ProtocolMessage pendingRequest = pendingResponse.computeIfAbsent(request.getCommand(),
                aShort -> new ProtocolMessage(request));
        Instant start = Instant.now();
        writeMessage(request);
        synchronized (pendingRequest) {
            pendingRequest.wait(timeout);
            pendingResponse.remove(request.getCommand());
            log.trace("Response from station took {}", DurationFormatUtils.formatDurationHMS(Duration.between(start, Instant.now()).toMillis()));
            return pendingRequest.getResponse();
        }
    }

    private void writeOutputStream(byte[] byteArrayMessage) throws IOException {
        ByteBuffer arrayWithLeadingLength = ByteBuffer.allocate(2 + byteArrayMessage.length);
        putUnsignedShort(arrayWithLeadingLength, byteArrayMessage.length);
        arrayWithLeadingLength.put(byteArrayMessage);

        out.write(arrayWithLeadingLength.array());
        out.flush();
        log.debug("Writing message to client {}", HexDecimalConverter.toHexString(byteArrayMessage));
    }

    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @RequiredArgsConstructor
    public static class ProtocolMessage {
        @Getter
        private final ProtocolEntity<?> request;
        @Setter
        @Getter
        private ProtocolEntity<RawMessage> response;
    }

}
