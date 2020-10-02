package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentConfirmationDto {
    private String stationId;
    private int holdAmount;
    private int bonusAmount;
    private int powerLevel;
}
