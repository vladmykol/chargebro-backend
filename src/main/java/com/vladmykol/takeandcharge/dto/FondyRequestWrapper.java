package com.vladmykol.takeandcharge.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Getter
@RequiredArgsConstructor
@ToString
public class FondyRequestWrapper {
    private final FondyRequest request;
}
