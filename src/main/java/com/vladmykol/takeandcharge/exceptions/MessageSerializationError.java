package com.vladmykol.takeandcharge.exceptions;

public abstract class MessageSerializationError extends RuntimeException{
    public MessageSerializationError(String message) {
        super(message);
    }

    public MessageSerializationError(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageSerializationError(){
    }
}
