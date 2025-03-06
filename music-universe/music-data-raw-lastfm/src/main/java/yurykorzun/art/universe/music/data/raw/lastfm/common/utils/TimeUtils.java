package yurykorzun.art.universe.music.data.raw.lastfm.common.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    private TimeUtils() {
        // no instance
    }

    public static Instant truncToDays(Instant instant) {
        return truncTo(instant, ChronoUnit.DAYS);
    }

    public static Instant truncTo(Instant instant, ChronoUnit unit) {
        return instant.truncatedTo(unit);
    }
}
