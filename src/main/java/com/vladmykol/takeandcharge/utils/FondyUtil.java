package com.vladmykol.takeandcharge.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class FondyUtil {

    public static String generateSignature(Object object, String privateKey) {
        Map<String, String> valueList = getFieldsForSignature(object);
        TreeMap<String, String> sortedByKey = new TreeMap<>(valueList);
        var joinedString = privateKey + "|" + String.join("|", sortedByKey.values());
        log.trace("Build signature from string: {}", joinedString);
        return (sha1(joinedString));
    }


    private static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.reset();
            msdDigest.update(input.getBytes(StandardCharsets.UTF_8), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can't calc SHA-1 hash", e);
        }
        return sha1;
    }

    public static Map<String, String> getFieldsForSignature(Object object) {
        var result = new HashMap<String, String>();
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            if (isNotSignatureField(field.getName())) {
                ReflectionUtils.makeAccessible(field);
                var value = String.valueOf(ReflectionUtils.getField(field, object));
                if (isNotEmptyString(value)) {
                    result.put(field.getName(), value);
                }
            }
        });
        return result;
    }

    private static boolean isNotEmptyString(String value) {
        return StringUtils.isNotEmpty(value) && !"null".equalsIgnoreCase(value);
    }

    private static boolean isNotSignatureField(String fieldName) {
        return !"signature".equalsIgnoreCase(fieldName);
    }


}

