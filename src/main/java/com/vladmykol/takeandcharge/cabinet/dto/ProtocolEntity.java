package com.vladmykol.takeandcharge.cabinet.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ProtocolEntity<T> {

    private final MessageHeader header;
    private final T body;

    public ProtocolEntity(MessageHeader header, T body) {
        this.header = header;
        this.body = body;
    }

    public ProtocolEntity(MessageHeader.MessageCommand headerCommand, T body) {
        this.header = new MessageHeader(headerCommand);
        this.body = body;
    }

    public ProtocolEntity(MessageHeader.MessageCommand headerCommand) {
        this.header = new MessageHeader(headerCommand);
        this.body = null;
    }

    public boolean hasBody() {
        return body != null;
    }

    public short getCommand() {
        return header.getCommand();
    }

}
