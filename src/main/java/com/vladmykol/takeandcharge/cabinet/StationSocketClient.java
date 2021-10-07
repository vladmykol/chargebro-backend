package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.ClientInfo;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import com.vladmykol.takeandcharge.exceptions.NoResponseFromWithinTimeout;
import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import com.vladmykol.takeandcharge.utils.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.util.*;

import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.*;


@Slf4j
public class StationSocketClient {
    @Getter
    private final ClientInfo clientInfo;
    @Getter
    private final List<ProtocolMessage> messageQueue = Collections.synchronizedList(new LinkedList<>());
    private final DataOutputStream outputStream;
    @Getter
    private final DataInputStream inputStream;
    private final Socket socket;
    @Getter
    private volatile boolean isActive = true;

    public StationSocketClient(Socket socket, int idleTimeoutSeconds) throws IOException {
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(10000);
        this.socket = socket;
        this.clientInfo = new ClientInfo(socket.getInetAddress(), idleTimeoutSeconds);
        this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 10 * 1024));
        log.debug("New station socket client {}", clientInfo);
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
        if (clientInfo.getShutdownTime() == null) {
            log.debug("Shutdown socket client {}", clientInfo, reason);
            clientInfo.setShutdownTime(Instant.now());
        }
        try {
            inputStream.close();
        } catch (Exception ignore) {
        }
        try {
            outputStream.close();
        } catch (Exception ignore) {
        }
        try {
            socket.close();
        } catch (Exception ignore) {
        }
    }

    public void check() {
        log.debug("Send check command to not responsive station {}", clientInfo);
        try {
            internalCommunicate(new ProtocolEntity<>(HEART_BEAT), 15000);
        } catch (NoResponseFromWithinTimeout e) {
            internalCommunicate(new ProtocolEntity<>(RESTART), 30000);
            throw new CabinetIsOffline();
        }
        log.debug("Success check command to not responsive station {}", clientInfo);
    }

    public ProtocolEntity<RawMessage> communicate(ProtocolEntity<?> request) {
        return internalCommunicate(request, 15000);
    }

    @SneakyThrows
    private ProtocolEntity<RawMessage> internalCommunicate(ProtocolEntity<?> request, int timeout) {
        ProtocolEntity<RawMessage> incomingMessage = writeAndWaitForResponse(request, timeout);

        if (incomingMessage == null) {
            throw new NoResponseFromWithinTimeout(timeout);
        } else
            return incomingMessage;
    }

    @SneakyThrows
    public void writeMessage(ProtocolEntity<?> protocolEntity) {
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
            throws InterruptedException {
        // TODO: 10/5/2020 same message ids can come and that one will be missed
        var protocolMessage = new ProtocolMessage(request.getCommand(), request);
        messageQueue.add(protocolMessage);
        Instant start = Instant.now();
        writeMessage(request);
        synchronized (protocolMessage) {
            protocolMessage.wait(timeout);
            messageQueue.remove(protocolMessage);
            if (protocolMessage.getResponse() != null) {
                log.trace("Response from station {} took {}", getClientInfo(), TimeUtils.timeSince(start));
            }
            return protocolMessage.getResponse();
        }
    }


    private void writeOutputStream(byte[] byteArrayMessage) throws IOException {
        log.trace("Writing message to station. Message content {}", HexDecimalConverter.toHexString(byteArrayMessage));
        outputStream.writeShort(byteArrayMessage.length);
        outputStream.write(byteArrayMessage);
        outputStream.flush();
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
