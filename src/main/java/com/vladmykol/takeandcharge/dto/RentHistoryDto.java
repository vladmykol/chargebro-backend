package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentHistoryDto {
    private String powerBankId;
    private long rentPeriodMs;
    private int isReturned;
    private int errorCode;
    private String errorMessage;
}
