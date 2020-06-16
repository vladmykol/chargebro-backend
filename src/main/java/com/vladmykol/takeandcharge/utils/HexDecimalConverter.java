package com.vladmykol.takeandcharge.utils;


import org.apache.tomcat.util.buf.HexUtils;

public class HexDecimalConverter {

    public static String toHexString(byte[] source) {
        return source != null ? HexUtils.toHexString(source).replaceAll("(?<=\\G..)", " ") : "";
    }

    private static String decimalToHexadecimal(byte b) {
        return String.format("%02x ", b);
    }
}

