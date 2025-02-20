package yurykorzun.art.universe.music.data.raw.lastfm.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeTestUtils {

    private TimeTestUtils() {
        // no instance
    }

    /**
     * Truncates provided {@link Instant} to millis to eliminate precision loss when saving to/loading from db.
     */
    public static Instant truncateToMillis(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * @return {@link Instant} truncated to millis to eliminate precision loss when saving to/loading from db.
     */
    public static Instant now() {
        return truncateToMillis(Instant.now());
    }

}
