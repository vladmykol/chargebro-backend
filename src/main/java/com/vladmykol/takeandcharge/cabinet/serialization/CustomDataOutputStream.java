package com.vladmykol.takeandcharge.cabinet.serialization;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class CustomDataOutputStream extends DataOutputStream {
    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter {@code written} is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see FilterOutputStream#out
     */
    public CustomDataOutputStream(OutputStream out) {
        super(out);
    }

    public void writeDate(Instant value) throws IOException {
        writeInt((int) value.getEpochSecond());
    }

    public void writeString(String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeShort(bytes.length + 1);
        write(bytes);
        write(0x00);
    }

    public void writeInt(long value) throws IOException {
        writeInt((int) (value & 0xffffffffL));
    }

    public void write8ByteLongString(String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        write(bytes);
    }
}
