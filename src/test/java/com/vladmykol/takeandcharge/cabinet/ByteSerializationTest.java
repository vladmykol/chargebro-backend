package com.vladmykol.takeandcharge.cabinet;

import com.vladmykol.takeandcharge.cabinet.dto.client.ReturnPowerBankRequest;
import com.vladmykol.takeandcharge.cabinet.serialization.CustomDataInputStream;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolSerializationUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Slf4j
public class ByteSerializationTest {

    @Test
    void serializationOfUnsignedValues() {
        //given
        TestMessage originTestMessage = new TestMessage.Builder()
                .withMaxUnsignedByte()
                .withMaxUnsignedShort()
                .withMaxUnsignedInt()
                .withSomeDate()
                .withSomeMsg()
                .build();
        byte[] expectedByteArray = {(byte) 0xfe, (byte) 0xff, (byte) 0xfe, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xfe, (byte) 0x5e, (byte) 0xa1, (byte) 0xcf, 0x43, 0x00, 0x14, 0x76, 0x65, 0x72, 0x79, 0x20, 0x69,
                0x6d, 0x70, 0x6f, 0x72, 0x74, 0x61, 0x6e, 0x74, 0x20, 0x74, 0x65, 0x78, 0x74, 0x00};
        //when
        byte[] serializedTestMessage = ProtocolSerializationUtils.serialize(originTestMessage);
        //than
        assertThat(serializedTestMessage, is(expectedByteArray));
    }

    @Test
    void serializationOfCoupleMessages() {
        //given
        TestMessage header = new TestMessage.Builder()
                .withUnsignedByte((short) 1)
                .build();
        TestMessage body = new TestMessage.Builder()
                .withUnsignedByte((short) 2)
                .build();
        byte[] expectedByteArray = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        //when
        byte[] serializedTestMessage = ProtocolSerializationUtils.serialize(header, body);
        //than
        assertThat(serializedTestMessage, is(expectedByteArray));
    }

    @Test
    void deserializationOfUnsignedValues() {
        //given
        byte[] originByteArrayTestMessage = {(byte) 0xfe, (byte) 0xff, (byte) 0xfe, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xfe, (byte) 0x5e, (byte) 0xa1, (byte) 0xcf, 0x43, 0x00, 0x14, 0x76, 0x65, 0x72, 0x79, 0x20, 0x69,
                0x6d, 0x70, 0x6f, 0x72, 0x74, 0x61, 0x6e, 0x74, 0x20, 0x74, 0x65, 0x78, 0x74, 0x00};
        TestMessage expectedTestMessage = new TestMessage.Builder()
                .withMaxUnsignedByte()
                .withMaxUnsignedShort()
                .withMaxUnsignedInt()
                .withSomeDate()
                .withSomeMsg()
                .build();
        //when
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(originByteArrayTestMessage);
        CustomDataInputStream dataInputStream = new CustomDataInputStream(byteArrayInputStream);
        TestMessage deserializeTestMessage = ProtocolSerializationUtils.readObject(dataInputStream, new TestMessage());
        //than
        assertThat(deserializeTestMessage, is(expectedTestMessage));
    }

    @Test
    void deserializeAndSerializeObjectsMatch() throws IOException {
        //given
        ReturnPowerBankRequest originMessage = new ReturnPowerBankRequest();
        originMessage.setSlotNumber((short) 3);
        originMessage.setPowerBankId("STWA12231245");
        //when
        byte[] serializedMessage = ProtocolSerializationUtils.serialize(originMessage);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedMessage);
        CustomDataInputStream dataInputStream = new CustomDataInputStream(byteArrayInputStream);
        ReturnPowerBankRequest deserializeMessage = ProtocolSerializationUtils.readObject(dataInputStream, new ReturnPowerBankRequest());
        //than
        assertThat(deserializeMessage, is(originMessage));
    }

    @Data
    public static class TestMessage {

        @ProtocolField(position = 1, dataType = BYTE)
        private short unsignedByte;

        @ProtocolField(position = 2, dataType = UINT16)
        private int unsignedShort;

        @ProtocolField(position = 3, dataType = UINT32)
        private long unsignedInt;

        @ProtocolField(position = 4, dataType = DATE)
        private Instant time;

        @ProtocolField(position = 5, dataType = STRING)
        private String msg;

        public static class Builder {
            private final TestMessage testMessage;

            public Builder() {
                this.testMessage = new TestMessage();
            }

            public Builder withMaxUnsignedByte() {
                testMessage.unsignedByte = (short) Byte.MAX_VALUE * 2;
                return this;
            }

            public Builder withUnsignedByte(short value) {
                testMessage.unsignedByte = value;
                return this;
            }

            public Builder withMaxUnsignedShort() {
                testMessage.unsignedShort = (int) Short.MAX_VALUE * 2;
                return this;
            }

            public Builder withMaxUnsignedInt() {
                testMessage.unsignedInt = (long) Integer.MAX_VALUE * 2;
                return this;
            }

            public Builder withSomeDate() {
                testMessage.time = Instant.ofEpochSecond(1587662659);
                return this;
            }

            public Builder withSomeMsg() {
                testMessage.msg = "very important text";
                return this;
            }

            public TestMessage build() {
                return testMessage;
            }
        }
    }
}

