package com.vladmykol.takeandcharge.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserIsFrozen extends RuntimeException {
    private final int frozenForSec;
}
