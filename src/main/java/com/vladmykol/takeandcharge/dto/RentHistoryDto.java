package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentHistoryDto {
    private String powerBankId;
    private Long rentPeriodMs;
    private int price;
    private boolean isActive;
    private Integer errorCode;
    private String errorMessage;
}
