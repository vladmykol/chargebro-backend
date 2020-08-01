package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class SendSmsRequestDto {
    private List<String> recipients;
    private SendSmsBodyDto sms;
    private SendSmsBodyDto viber;
}
