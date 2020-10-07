package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AuthenticatedStationsDto {
    private final List<String> sessionDuration = new ArrayList<>();
    private String stationId;
    private boolean isActive;
    private Instant lastSeen;
}
