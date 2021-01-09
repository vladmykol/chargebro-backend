package com.vladmykol.takeandcharge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AuthenticatedStationsDto {
    private final List<String> pastSessions;
    private final String timeSinceLastLogIn;
    private String stationId;
    private boolean isOnline;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Kiev")
    private Instant lastSeen;
}
