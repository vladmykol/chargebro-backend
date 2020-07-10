package com.vladmykol.takeandcharge.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendSmsResponseDto {
    private int response_code;
    private String response_status;
    private List<SendSmsResultDto> response_result;
}
