package com.vladmykol.takeandcharge.exceptions;

public class UserAlreadyExist extends RuntimeException {
    public UserAlreadyExist() {
        super("User already exists");
    }
}
