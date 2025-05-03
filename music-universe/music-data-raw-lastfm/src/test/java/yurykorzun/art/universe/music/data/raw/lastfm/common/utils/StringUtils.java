package yurykorzun.art.universe.music.data.raw.lastfm.common.utils;

import java.util.UUID;

public class StringUtils {

    private StringUtils() {
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
