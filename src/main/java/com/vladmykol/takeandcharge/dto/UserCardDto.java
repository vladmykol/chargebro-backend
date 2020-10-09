package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class UserCardDto {
    private final String id;
    private final String type;
    private final String maskedNum;
}
