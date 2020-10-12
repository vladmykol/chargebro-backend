package com.vladmykol.takeandcharge.exceptions;

public class UserIsBlocked extends RuntimeException {
    public UserIsBlocked(){
        super("User is blocked");
    }
}
