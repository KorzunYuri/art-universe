package yurykorzun.art.universe.music.data.raw.lastfm.common;

import java.time.LocalDate;

public class LastfmConstants {

    public static final String DATA_SOURCE_CODE = "lastfm";

    public static final int HIBERNATE_BATCH_SIZE = 50;

    public static final LocalDate END_OF_TIME = LocalDate.of(9999, 12, 31);

    public static final String NOTIFY_CALLS_READY = "lastfm_calls_ready";
    public static final String NOTIFY_RESPONSES_READY = "lastfm_responses_ready";
}
