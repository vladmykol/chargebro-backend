package com.vladmykol.takeandcharge.dto;

import lombok.Data;

@Data
public class SendSmsResultDto {
    private String phone;
    private int response_code;
    private String message_id;
    private String response_status;
}
