package com.vladmykol.takeandcharge.utils;


import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;

public class TimeUtils {

    public static String timeSince(Instant start) {
        return timeBetween(start, Instant.now());
    }

    public static String timeBetween(Instant start, Instant end) {
        return DurationFormatUtils.formatDurationHMS(Duration.between(start, end).toMillis());
    }

    public static String timeBetweenWords(Instant start) {
        return DurationFormatUtils.formatDurationWords(Duration.between(start, Instant.now()).toMillis(), true, true);
    }

}

