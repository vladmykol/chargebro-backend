package com.vladmykol.takeandcharge.cabinet.serialization;

import com.vladmykol.takeandcharge.exceptions.IncompatibleFieldType;
import com.vladmykol.takeandcharge.exceptions.UnsupportedFieldType;
import lombok.SneakyThrows;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ProtocolSerializationUtils {

    private static final Class<ProtocolField> PROTOCOL_FIELD = ProtocolField.class;


    public static <T> T readObject(CustomDataInputStream source, T destination) {
        Set<Field> fields = getAnnotatedFields(destination.getClass());

        fields.stream()
                .sorted(ByPosition())
                .forEachOrdered(field -> {
                    writeToField(source, destination, field);
                });

        return destination;
    }

    public static byte[] serialize(Object... objects) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CustomDataOutputStream destination = new CustomDataOutputStream(byteArrayOutputStream);

        for (Object oneObject : objects) {
            Set<Field> fields = getAnnotatedFields(oneObject.getClass());

            fields.stream()
                    .sorted(ByPosition())
                    .forEachOrdered(field -> {
                        readFromField(oneObject, destination, field);
                    });
        }

        return byteArrayOutputStream.toByteArray();
    }

    private static Comparator<Field> ByPosition() {
        return Comparator.comparingInt(o -> o.getAnnotation(PROTOCOL_FIELD).position());
    }

    @SneakyThrows
    private static void writeToField(CustomDataInputStream source, Object instance, Field field) {
        Object value = readValueByType(source, field.getAnnotation(PROTOCOL_FIELD).dataType());

        setField(instance, field, value);
    }

    @SneakyThrows
    private static void readFromField(Object instance, CustomDataOutputStream destination, Field field) {
        Object value = getField(instance, field);

        try {
            writeValueByType(destination, field.getAnnotation(PROTOCOL_FIELD).dataType(), value);
        } catch (ClassCastException e) {
            throwIncompatibleException(instance, field, e);
        }
    }

    private static void throwIncompatibleException(Object instance, Field field, ClassCastException e) {
        String message = String.format("Incompatible declared<%1$s> and actual<%2$s> data types when trying to serialize %3$s",
                field.getAnnotation(PROTOCOL_FIELD).dataType(),
                field.getType().getSimpleName(),
                instance.getClass().getSimpleName() + "." + field.getName());
        throw new IncompatibleFieldType(message, e);
    }

    private static void setField(Object instance, Field field, Object value) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, instance, value);
    }

    private static Object getField(Object instance, Field field) {
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, instance);
    }

    private static Object readValueByType(CustomDataInputStream inputStream, DataType fieldType) throws IOException {
        Object value = null;

        if (inputStream.available() > 0)
            value = switch (fieldType) {
                case BYTE -> (short) inputStream.readUnsignedByte();
                case UINT16 -> inputStream.readUnsignedShort();
                case UINT32 -> inputStream.readUnsignedInt();
                case BYTE8 -> inputStream.readLong();
                case BYTE8STRING -> inputStream.read8ByteLongString();
                case DATE -> inputStream.readDate();
                case STRING -> inputStream.readString();
                default -> throw new UnsupportedFieldType(fieldType + " is not supported");
            };

        return value;
    }


    private static void writeValueByType(CustomDataOutputStream outputStream, DataType fieldType, Object value) throws IOException {
        if (value != null)
            switch (fieldType) {
                case BYTE -> outputStream.writeByte((short) value);
                case UINT16 -> outputStream.writeShort((int) value);
                case UINT32 -> outputStream.writeInt((long) value);
                case BYTE8 -> outputStream.writeLong((long) value);
                case BYTE8STRING -> outputStream.write8ByteLongString((String) value);
                case DATE -> outputStream.writeDate((Instant) value);
                case STRING -> outputStream.writeString((String) value);
                default -> throw new UnsupportedFieldType(fieldType + " is not supported");
            }

    }


    private static Set<Field> getAnnotatedFields(Class<?> clazz) {
        Set<Field> set = new HashSet<>();
        while (clazz != null) {
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(PROTOCOL_FIELD))
                    .forEach(set::add);
            clazz = clazz.getSuperclass();
        }
        return set;
    }

    ;
}
