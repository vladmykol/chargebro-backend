package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendSmsBodyDto {
    @Builder.Default
    private String sender = "Take&Charge";
    private String text;
}
