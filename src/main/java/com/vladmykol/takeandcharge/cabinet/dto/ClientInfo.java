package com.vladmykol.takeandcharge.cabinet.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.time.Instant;

@Data
@RequiredArgsConstructor
public class ClientInfo {

    private final InetAddress IpAddress;

    private String cabinetId;

    private Instant lastSeen = Instant.now().minusSeconds(25);
}
