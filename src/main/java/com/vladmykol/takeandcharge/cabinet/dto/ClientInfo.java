package com.vladmykol.takeandcharge.cabinet.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.time.Instant;

@EqualsAndHashCode
public class ClientInfo {

    private final InetAddress inetAddress;

    @Getter
    @Setter
    private String cabinetId;

    @Getter
    @Setter
    private Instant lastSeen;

    public ClientInfo(InetAddress inetAddress, int idleTimeoutSeconds) {
        this.inetAddress = inetAddress;
//        right after connection station should be monitored for active state
        this.lastSeen = Instant.now().minusSeconds(idleTimeoutSeconds - 3);
    }

    public String getName() {
        if (cabinetId == null) {
            return inetAddress.getHostAddress();
        } else {
            return cabinetId;
        }
    }
}
