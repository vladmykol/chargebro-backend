package com.vladmykol.takeandcharge.cabinet.dto;

import lombok.Getter;

public enum CommonResult {
    OK(1),
    ERROR(0);

    @Getter
    private final short value;

    CommonResult(int result) {
        this.value = (short) result;
    }
}
