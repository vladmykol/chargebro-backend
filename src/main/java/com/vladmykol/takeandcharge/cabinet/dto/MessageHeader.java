package com.vladmykol.takeandcharge.cabinet.dto;

import com.vladmykol.takeandcharge.cabinet.serialization.ProtocolField;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.BYTE;
import static com.vladmykol.takeandcharge.cabinet.serialization.DataType.DATE;

@Data
public class MessageHeader {

    @ProtocolField(position = 1, dataType = BYTE)
    private short command;

    @ProtocolField(position = 2, dataType = BYTE)
    private short protocolVersion = (short) 1;

    @ProtocolField(position = 3, dataType = BYTE)
    private short checkSum = 7;

    @ProtocolField(position = 4, dataType = DATE)
    private Instant time = Instant.now();

    public MessageHeader(MessageCommand commandEnum) {
        this.command = commandEnum.getCommand();
    }

    public MessageHeader() {
    }

    public enum MessageCommand {
        SING_IN(0X60),
        HEART_BEAT(0X61),
        SOFTWARE_VERSION(0X62),
        SET_SERVER_ADDRESS(0X63),
        CABINET_STOCK(0X64),
        TAKE_POWER_BANK(0X65),
        RETURN_POWER_BANK(0X66),
        RESTART(0X67),
        UPGRADE(0X68),
        SIM_INFO(0X69),
        GET_SERVER_ADDRESS(0X6A),
        GET_STOCK_NUMBER(0X6B),
        FORCE_POPUP(0X80),
        NOT_DEFINED(-1);

        private static final Map<Short, MessageCommand> BY_CODE_MAP = new HashMap<>();

        static {
            for (MessageCommand curEnum : MessageCommand.values()) {
                BY_CODE_MAP.put(curEnum.command, curEnum);
            }
        }

        @Getter
        private final short command;

        MessageCommand(int command) {
            this.command = (short) command;
        }

        public static MessageCommand byCommand(int command) {
            return BY_CODE_MAP.getOrDefault(command, MessageCommand.NOT_DEFINED);
        }
    }

}
