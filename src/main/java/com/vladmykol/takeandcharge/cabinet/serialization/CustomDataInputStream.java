package com.vladmykol.takeandcharge.cabinet.serialization;

import org.apache.tomcat.util.buf.HexUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class CustomDataInputStream extends DataInputStream {

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public CustomDataInputStream(InputStream in) {
        super(in);
    }

    public long readUnsignedInt() throws IOException {
        return ((long) readInt() & 0xffffffffL);
    }

    public Instant readDate() throws IOException {
        long l = readUnsignedInt();
        return Instant.ofEpochSecond(l);
    }

    public String readString() throws IOException {
        //trim to remove null-char at the end
        return readUTF().trim();
    }

    public String read8ByteLongString() throws IOException {
        byte[] str = readNBytes(4);
        byte[] number = readNBytes(4);

        return new String(str, StandardCharsets.UTF_8) + HexUtils.toHexString(number);
    }
}
