package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import com.vladmykol.takeandcharge.exceptions.NoResponseFromWithinTimeout;
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
import java.util.*;

import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.*;


@Slf4j
public class StationSocketClient {
    @Getter
    private final ClientInfo clientInfo;
    @Getter
    private final List<ProtocolMessage> messageQueue = Collections.synchronizedList(new LinkedList<>());
    private final OutputStream out;
    private final Socket socket;
    @Getter
    private volatile boolean isActive = true;
    @Getter
    @Setter
    private volatile Instant shutdownTime;


    public StationSocketClient(Socket socket, int idleTimeoutSeconds) throws IOException {
        socket.setKeepAlive(true);
        socket.setSendBufferSize(1);
        socket.setReceiveBufferSize(1);
        socket.setReuseAddress(true);
        socket.setSoTimeout(10000);
        socket.setTcpNoDelay(true);
        this.socket = socket;
        this.clientInfo = new ClientInfo(socket.getInetAddress(), idleTimeoutSeconds);
        this.out = socket.getOutputStream();
        log.debug("New station socket client {}", clientInfo);
    }

    public static void putUnsignedShort(ByteBuffer bb, int value) {
        bb.putShort((short) (value & 0xffff));
    }

    public void setActive() {
        isActive = true;
    }

    public void setInactive() {
        isActive = false;
    }

    public boolean isSocketConnected() {
        return !socket.isClosed();
    }

    @SneakyThrows
    public synchronized void shutdown(Exception reason) {
        isActive = false;
        if (shutdownTime == null) {
            log.debug("Shutdown socket client {}", clientInfo, reason);
            shutdownTime = Instant.now();
        }
        try {
            out.close();
        } catch (Exception ignore) {
        }
        try {
            socket.close();
        } catch (Exception ignore) {
        }
    }

    @SneakyThrows
    public void ping() {
        ProtocolEntity<?> softwareVersionRequest = new ProtocolEntity<>(HEART_BEAT);
        writeMessage(softwareVersionRequest);
    }

    public boolean isResponsive() {
        ProtocolEntity<?> softwareVersionRequest = new ProtocolEntity<>(SOFTWARE_VERSION);
        log.debug("Check if station responsive {}", clientInfo);
        try {
            ping();
            communicate(softwareVersionRequest, 10000);
        } catch (NoResponseFromWithinTimeout e) {
            if (isSocketConnected()) {
                log.debug("Second try for check if station responsive {}", clientInfo);
                try {
                    communicate(softwareVersionRequest, 10000);
                } catch (NoResponseFromWithinTimeout e2) {
                    if (isSocketConnected()) {
                        log.debug("Send restart command to not responsive station {}", clientInfo);
                        writeMessage(new ProtocolEntity<>(RESTART));
                    }
                    throw new CabinetIsOffline();
                }
            } else {
                throw new CabinetIsOffline();
            }
        }
        log.debug("Success check if station responsive {}", clientInfo);

        return true;
    }

    public ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request) {
        return communicate(request, 20000);
    }

    @SneakyThrows
    private ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request, int timeout) {
        ProtocolEntity<RawMessage> incomingMessage = writeAndWaitForResponse(request, timeout);

        if (incomingMessage == null)
            throw new NoResponseFromWithinTimeout(timeout);
        else
            return incomingMessage;
    }

    @SneakyThrows
    public synchronized void writeMessage(ProtocolEntity<?> protocolEntity) {
        byte[] byteArrayMessage;
        if (protocolEntity.hasBody()) {
            byteArrayMessage = ProtocolSerializationUtils.serialize(protocolEntity.getHeader(), protocolEntity.getBody());
        } else {
            byteArrayMessage = ProtocolSerializationUtils.serialize(protocolEntity.getHeader());
        }

        writeOutputStream(byteArrayMessage);
    }

    public boolean fulfillPendingRequest(ProtocolEntity<RawMessage> incomingMessage) {
        Optional<ProtocolMessage> optionalProtocolMessage = messageQueue.stream()
                .filter(protocolMessage -> protocolMessage.getMessageType() == incomingMessage.getCommand())
                .findFirst();

        if (optionalProtocolMessage.isPresent()) {
            synchronized (optionalProtocolMessage.get()) {
                optionalProtocolMessage.get().setResponse(incomingMessage);
                optionalProtocolMessage.get().notify();
            }
        }
        return optionalProtocolMessage.isPresent();
    }

    private ProtocolEntity<RawMessage> writeAndWaitForResponse(ProtocolEntity<?> request, int timeout)
            throws InterruptedException, IOException {
        // TODO: 10/5/2020 same message ids can come and that one will be missed
        var protocolMessage = new ProtocolMessage(request.getCommand(), request);
        messageQueue.add(protocolMessage);
        Instant start = Instant.now();
        writeMessage(request);
        synchronized (protocolMessage) {
            protocolMessage.wait(timeout);
            messageQueue.remove(protocolMessage);
            log.trace("Response from station {} took {}", getClientInfo(),
                    DurationFormatUtils.formatDurationHMS(Duration.between(start, Instant.now()).toMillis()));
            return protocolMessage.getResponse();
        }
    }

    private void writeOutputStream(byte[] byteArrayMessage) throws IOException {
        ByteBuffer arrayWithLeadingLength = ByteBuffer.allocate(2 + byteArrayMessage.length);
        putUnsignedShort(arrayWithLeadingLength, byteArrayMessage.length);
        arrayWithLeadingLength.put(byteArrayMessage);

        log.trace("Writing message to station. Message content {}", HexDecimalConverter.toHexString(byteArrayMessage));
        out.write(arrayWithLeadingLength.array());
        out.flush();
    }

    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationSocketClient that = (StationSocketClient) o;
        return socket.equals(that.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket);
    }

    @RequiredArgsConstructor
    public static class ProtocolMessage {
        @Getter
        private final short messageType;
        @Getter
        private final ProtocolEntity<?> request;
        @Setter
        @Getter
        private ProtocolEntity<RawMessage> response;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProtocolMessage that = (ProtocolMessage) o;
            return messageType == that.messageType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageType);
        }
    }

}
