package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeUtil {

    private TimeUtil() {}

    public static Instant calcDueDttm(int dueDays) {
        return Instant.now()
                .plus(Duration.ofDays(dueDays))
                .truncatedTo(ChronoUnit.DAYS)
                .minus(Duration.ofMillis(1));
    }


}
