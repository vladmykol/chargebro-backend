package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.HEART_BEAT;
import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.SOFTWARE_VERSION;


@Slf4j
public class StationSocketClient {
    @Getter
    private final ClientInfo clientInfo;
    private final Socket socket;
    @Getter
    private final Map<Short, ProtocolMessage> pendingResponse = Collections.synchronizedMap(new HashMap<>());
    private final OutputStream out;
    @Setter
    @Getter
    private volatile boolean isActive = true;

    public StationSocketClient(Socket socket, int idleTimeoutSeconds) throws IOException {
        this.socket = socket;
        this.clientInfo = new ClientInfo(socket.getInetAddress(), idleTimeoutSeconds);
        this.out = socket.getOutputStream();
        log.debug("Station socket client {} is now connected", clientInfo.getInetAddress());
    }

    public static void putUnsignedShort(ByteBuffer bb, int value) {
        bb.putShort((short) (value & 0xffff));
    }

    @SneakyThrows
    public void shutdown(Exception reason) {
        isActive = false;
        if (!socket.isClosed()) {
            socket.close();
            log.debug("Client {} is now disconnected", clientInfo.getInetAddress(), reason);
        }
    }

    public void ping() throws IOException {
        ProtocolEntity<?> softwareVersionRequest = new ProtocolEntity<>(HEART_BEAT);
        writeMessage(softwareVersionRequest);
    }

    public void check() {
        ProtocolEntity<?> softwareVersionRequest = new ProtocolEntity<>(SOFTWARE_VERSION);
        communicate(softwareVersionRequest, 10000);
    }

    public ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request) {
        return communicate(request, 20000);
    }

    @SneakyThrows
    private ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request, int timeout) {
        ProtocolEntity<RawMessage> incomingMessage = waitAndGetResponse(request, timeout);

        if (incomingMessage == null)
            throw new TimeoutException("no response from a client");
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

    private synchronized void writeOutputStream(byte[] byteArrayMessage) throws IOException {
        ByteBuffer arrayWithLeadingLength = ByteBuffer.allocate(2 + byteArrayMessage.length);
        putUnsignedShort(arrayWithLeadingLength, byteArrayMessage.length);
        arrayWithLeadingLength.put(byteArrayMessage);

        out.write(arrayWithLeadingLength.array());
        log.trace("Writing message to client {}", HexDecimalConverter.toHexString(byteArrayMessage));
    }

    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationSocketClient that = (StationSocketClient) o;
        return clientInfo.getInetAddress().equals(that.clientInfo.getInetAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientInfo.getInetAddress());
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
