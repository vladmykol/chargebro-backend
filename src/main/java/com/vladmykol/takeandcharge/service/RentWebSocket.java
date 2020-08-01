package com.vladmykol.takeandcharge.service;


import com.vladmykol.takeandcharge.security.TokenService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentWebSocket extends BinaryWebSocketHandler {
    private static final short MESSAGE_TYPE_AUTH = 1;
    private static final short MESSAGE_TYPE_UPDATE = 2;
    private static final short MESSAGE_CODE_OK = 200;
    private static final short MESSAGE_CODE_UNAUTHORIZED = 401;
    private static final MultiValuedMap<String, WebSocketSession> clientIdAndConnections = new HashSetValuedHashMap<>();
    private final TokenService tokenService;


    public void sendPowerBankReturnedMessage(String clientId, String powerBankId) {
        final var returnedPowerBank = BaseMessage.builder()
                .messageType(MESSAGE_TYPE_UPDATE)
                .messageCode(MESSAGE_CODE_OK)
                .message(powerBankId)
                .build();

        Collection<WebSocketSession> webSocketSessions = clientIdAndConnections.get(clientId);
        if (webSocketSessions != null) {
            webSocketSessions.forEach(webSocketSession -> {
                try {
                    sendMessage(webSocketSession, returnedPowerBank);
                    log.debug("Sent rent update to web socket client {}", webSocketSession.getRemoteAddress());
                } catch (IOException e) {
                    log.error("Cannon send rent update to web socket client {}", webSocketSession.getRemoteAddress(), e);
                }
            });
        } else {
            log.debug("No connected web socket clients to send rent update to");
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("Client {} is now connected", session.getRemoteAddress());
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
            String clientId = tokenService.parseAuthToken(tokenBuilder.toString());
            synchronized (clientIdAndConnections) {
                if (clientIdAndConnections.put(clientId, session))
                    log.debug("Client {} is now authenticated", session.getRemoteAddress());
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
                    iterator.remove();
                }
            }
        }
        System.out.println("client disconnected. Remaining clients " + clientIdAndConnections);
    }


    @Builder
    @Getter
    private static class BaseMessage {
        private final short messageType;
        private final short messageCode;
        private final String message;
    }
}
