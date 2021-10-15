package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class DisconnectedStationsDto {
    private final String stationId;
    private final String timeSinceDisconnected;
}
