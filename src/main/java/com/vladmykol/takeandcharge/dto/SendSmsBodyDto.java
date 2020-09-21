package com.vladmykol.takeandcharge.dto;

import lombok.Data;

@Data
public class SendSmsBodyDto {
    private String sender = "Take&Charge";
    private String text;

    public SendSmsBodyDto(String validationCode) {
        this.text = sender + " code: " + validationCode;
    }
}
