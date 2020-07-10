package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SingUpDto {
    private String token;
    private String smsCode;
    private String name;
    private String password;
}
