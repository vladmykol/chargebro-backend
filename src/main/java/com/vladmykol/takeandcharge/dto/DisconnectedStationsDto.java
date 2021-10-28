package com.vladmykol.takeandcharge.dto;

import lombok.Data;

@Data
public class DisconnectedStationsDto {
    private final String stationId;
    private final String timeSinceDisconnected;
}
