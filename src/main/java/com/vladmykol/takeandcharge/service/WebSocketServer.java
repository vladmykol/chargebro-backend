package com.vladmykol.takeandcharge.service;


import com.vladmykol.takeandcharge.exceptions.HttpException;
import com.vladmykol.takeandcharge.security.JwtProvider;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServer extends BinaryWebSocketHandler {
    private static final short MESSAGE_TYPE_AUTH = 1;
    private static final short MESSAGE_TYPE_RENT_START = 2;
    private static final short MESSAGE_TYPE_RENT_END = 3;
    private static final short MESSAGE_TYPE_ERROR = 4;
    private static final short MESSAGE_TYPE_RENT_MONEY_HOLD_CONFIRM = 5;
    private static final short MESSAGE_TYPE_RESOLVE_STATION_CONNECTION_ISSUE = 6;
    private static final short MESSAGE_CODE_OK = 200;
    private static final int MESSAGE_CODE_PAYMENT_ERROR = HttpStatus.PAYMENT_REQUIRED.value();
    private static final short MESSAGE_CODE_UNAUTHORIZED = 401;
    private static final short MESSAGE_CODE_GENERAL_ERROR = 500;
    private static final MultiValuedMap<String, WebSocketSession> clientIdAndConnections = new HashSetValuedHashMap<>();
    private final JwtProvider jwtProvider;

    public Map<String, List<String>> getConnectedClients() {
        Map<String, List<String>> result = new HashMap<>();

        clientIdAndConnections.entries().forEach(entry -> {
            var resultSessions = result.get(entry.getKey());
            if (resultSessions == null) {
                resultSessions = new ArrayList<>();
                result.putIfAbsent(entry.getKey(), resultSessions);
            }
            resultSessions.add(entry.getValue().getRemoteAddress().getAddress().toString());
        });
        return result;
    }

    public void sendRentStartMessage(String powerBankId) {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_RENT_START)
                .messageCode(MESSAGE_CODE_OK)
                .message(powerBankId)
                .build();

        sendBaseMassage(baseMessage);
    }

    public void sendResolveConnectionIssue() {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_RESOLVE_STATION_CONNECTION_ISSUE)
                .messageCode(MESSAGE_CODE_OK)
                .message("")
                .build();

        sendBaseMassage(baseMessage);
    }

    public void sendMoneyHoldConfirmationMessage(String status) {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_RENT_MONEY_HOLD_CONFIRM)
                .messageCode(MESSAGE_CODE_OK)
                .message(status)
                .build();

        sendBaseMassage(baseMessage);
    }

    public void sendRentEndMessage(String powerBankId) {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_RENT_END)
                .messageCode(MESSAGE_CODE_OK)
                .message(powerBankId)
                .build();

        sendBaseMassage(baseMessage);
    }

    public void sendErrorMessage(HttpException rentException) {
        sendErrorMessage(rentException.getStatus().value(), rentException.getMessage());
    }

    public void sendErrorMessage(int errorCode, String errorMessage) {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_ERROR)
                .messageCode(errorCode)
                .message(errorMessage)
                .build();

        sendBaseMassage(baseMessage);
    }


    public void sendPaymentErrorMessage(String errorMessage) {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_ERROR)
                .messageCode(MESSAGE_CODE_PAYMENT_ERROR)
                .message(errorMessage)
                .build();

        sendBaseMassage(baseMessage);
    }

    public void sendGeneralErrorMessage(String errorMessage) {
        final var baseMessage = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_ERROR)
                .messageCode(MESSAGE_CODE_GENERAL_ERROR)
                .message(errorMessage)
                .build();

        sendBaseMassage(baseMessage);
    }

    private void sendBaseMassage(BaseMessage baseMessage) {
        var currentUserId = SecurityUtil.getUser();
        Collection<WebSocketSession> webSocketSessions = clientIdAndConnections.get(currentUserId);
        if (webSocketSessions != null && !webSocketSessions.isEmpty()) {
            webSocketSessions.forEach(webSocketSession -> {
                try {
                    sendMessage(webSocketSession, baseMessage);
                    log.debug("Send rent update {} to web socket client {}", baseMessage, webSocketSession.getRemoteAddress());
                } catch (IOException e) {
                    log.error("Cannon send rent update to web socket client {}", webSocketSession.getRemoteAddress(), e);
                }
            });
        } else {
            log.warn("No connected web socket clients to send rent update {}",baseMessage);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.trace("Web Socket Client {} is now connected", session.getRemoteAddress());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session,
                                       BinaryMessage message) throws Exception {
        ByteBuffer messagePayload = message.getPayload();

        short messageType = messagePayload.getShort();

        switch (messageType) {
            case MESSAGE_TYPE_AUTH:
                authenticate(session, messagePayload);
                break;
//            case MESSAGE_TYPE_UPDATE:
//                returnPowerBankAction(message);
//                break;
            default:
                System.out.println("not defined message type  " + messageType + " from client");
        }
    }

    private void authenticate(WebSocketSession session, ByteBuffer messagePayload) throws IOException {
        short stringLength = messagePayload.getShort();
        StringBuilder tokenBuilder = new StringBuilder();
        for (int iter = 0; iter < stringLength; iter++) {
            tokenBuilder.append((char) messagePayload.get());
        }

        try {
            String clientId = jwtProvider.parseAuthToken(tokenBuilder.toString());
            synchronized (clientIdAndConnections) {
                if (clientIdAndConnections.put(clientId, session))
                    log.trace("Web Socket Client {} is now authenticated", session.getRemoteAddress());
            }
            final var successAuth = BaseMessage.builder()
                    .messageType(MESSAGE_TYPE_AUTH)
                    .messageCode(MESSAGE_CODE_OK)
                    .message("all good")
                    .build();
            sendMessage(session, successAuth);
        } catch (Exception e) {
            final var invalidToken = BaseMessage.builder()
                    .messageType(MESSAGE_TYPE_AUTH)
                    .messageCode(MESSAGE_CODE_UNAUTHORIZED)
                    .message("Invalid token")
                    .build();
            sendMessage(session, invalidToken);
            session.close();
        }
    }

    private void sendMessage(WebSocketSession session, BaseMessage baseMessage) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            write(baseMessage, dos);

            dos.flush();
            BinaryMessage bin = new BinaryMessage(bos.toByteArray());
            session.sendMessage(bin);
        }
    }

    private void write(BaseMessage baseMessage, DataOutputStream dos) throws IOException {
        dos.writeShort(baseMessage.getMessageType());
        dos.writeShort(baseMessage.getMessageCode());
        dos.writeUTF(baseMessage.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        synchronized (clientIdAndConnections) {
            MapIterator<String, WebSocketSession> iterator = clientIdAndConnections.mapIterator();
            while (iterator.hasNext()) {
                iterator.next();
                if (iterator.getValue().equals(session)) {
                    log.debug("WebSocket client is disconnected {}", iterator.getValue());
                    iterator.remove();
                }
            }
        }
    }


    @Builder
    @Getter
    @ToString
    private static class BaseMessage {
        private final short messageType;
        private final int messageCode;
        private final String message;
    }
}
