package com.vladmykol.takeandcharge.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SmsRegistrationTokenInfo {
    private final int validForMin;
    private final String code;
    private final String token;
}
