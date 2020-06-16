package com.vladmykol.takeandcharge.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RentResponse {

    private Instant rentTime;
}
