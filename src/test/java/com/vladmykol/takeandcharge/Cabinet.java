package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.serialization.CustomDataInputStream;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

@Slf4j
public class Cabinet {
    private Socket socket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;


    public Cabinet(String address, int port) throws IOException {
        socket = new Socket(address, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public byte[] communicate(byte[] message) throws IOException {
        out.write(message);
        out.flush();

        return readInputStream();
    }

    public void writeMessage(ProtocolEntity<?> protocolEntity) throws IOException {
        byte[] byteArrayMessage;
        if (protocolEntity.hasBody()) {
            byteArrayMessage = ProtocolSerializationUtils.serialize(protocolEntity.getHeader(), protocolEntity.getBody());
        } else {
            byteArrayMessage = ProtocolSerializationUtils.serialize(protocolEntity.getHeader());
        }

        writeOutputStream(byteArrayMessage);
    }

    public void writeOutputStream(byte[] byteArrayMessage) throws IOException {
        out.writeShort(byteArrayMessage.length);
        out.write(byteArrayMessage);
        out.flush();
    }

    public ProtocolEntity<RawMessage> readMessage() throws IOException {
        byte[] byteArray = readInputStream();

        CustomDataInputStream customDataInputStream = new CustomDataInputStream(new ByteArrayInputStream(byteArray));
        MessageHeader messageHeader = ProtocolSerializationUtils.readObject(customDataInputStream, new MessageHeader());
        return new ProtocolEntity<>(messageHeader, new RawMessage(customDataInputStream));
    }

    public byte[] readInputStream() throws IOException {
        int messageLength = in.readUnsignedShort();
        return in.readNBytes(messageLength);
    }

    public void disconnect() throws IOException {
        socket.close();
    }

}

