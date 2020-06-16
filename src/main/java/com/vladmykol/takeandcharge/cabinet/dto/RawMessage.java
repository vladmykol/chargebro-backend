package com.vladmykol.takeandcharge.cabinet.dto;

import com.vladmykol.takeandcharge.exceptions.NotAllBytesAreRead;
import com.vladmykol.takeandcharge.cabinet.serialization.CustomDataInputStream;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import com.vladmykol.takeandcharge.utils.HexDecimalConverter;
import lombok.SneakyThrows;

public class RawMessage {
    private final CustomDataInputStream customDataInputStream;

    public RawMessage(CustomDataInputStream customDataInputStream) {
        this.customDataInputStream = customDataInputStream;
    }

    @SneakyThrows
    public <T> T readTo(T object) {
        return ProtocolSerializationUtils.readObject(customDataInputStream, object);
    }

    @SneakyThrows
    public <T> T readFullyTo(T object) {
        T readObject = ProtocolSerializationUtils.readObject(customDataInputStream, object);
        if (customDataInputStream.available() > 0) throw new NotAllBytesAreRead();
        return readObject;
    }


    @SneakyThrows
    private String convertToHexString() {
        return HexDecimalConverter.toHexString(customDataInputStream.readAllBytes());
    }

    @Override
    public String toString() {
        return "RawMessage{" + convertToHexString() + '}';
    }
}
