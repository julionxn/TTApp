package me.julionxn.ttapp.util;

import java.time.*;

public class DatesUtil {

    public static LocalDateTime fromEpoch(long epoch) {
        return Instant.ofEpochSecond(epoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static long now() {
        return toEpoch(LocalDateTime.now());
    }

    public static long toEpoch(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
    }

}