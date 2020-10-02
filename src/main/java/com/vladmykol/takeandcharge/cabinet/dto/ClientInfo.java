package com.vladmykol.takeandcharge.cabinet.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.time.Instant;

@Data
@RequiredArgsConstructor
public class ClientInfo {

    private final InetAddress inetAddress;

    private String cabinetId;

    private Instant lastSeen;

    public ClientInfo(InetAddress inetAddress, int idleTimeoutSeconds) {
        this.inetAddress = inetAddress;
//        right after connection station should be monitored for active state
        this.lastSeen = Instant.now().minusSeconds(idleTimeoutSeconds - 3);
    }
}
