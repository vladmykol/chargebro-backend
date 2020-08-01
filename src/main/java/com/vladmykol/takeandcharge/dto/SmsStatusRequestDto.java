package com.vladmykol.takeandcharge.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class SmsStatusRequestDto {
    private List<String> messages = new ArrayList<>();

    public SmsStatusRequestDto(String messages) {
        this.messages = Collections.singletonList(messages);
    }
}
