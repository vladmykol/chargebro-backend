package com.vladmykol.takeandcharge.cabinet.serialization;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolField {
    int position();
    DataType dataType() default DataType.STRING;
}
