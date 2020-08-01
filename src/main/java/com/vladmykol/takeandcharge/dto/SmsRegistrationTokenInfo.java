package com.vladmykol.takeandcharge.dto;

import lombok.Data;

@Data
public class SmsRegistrationTokenInfo {
    private int validForMin;
    private String code;
    private String token;
    private String warningMessage;
}
