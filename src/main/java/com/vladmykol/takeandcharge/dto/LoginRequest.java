package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class LoginRequest {
    @NotNull
    private String username;

    @NotNull
    private String password;
}
