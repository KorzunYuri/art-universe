package yurykorzun.art.universe.music.data.raw.lastfm.common.utils;

import java.util.UUID;

public class TestStringUtils {

    private TestStringUtils() {
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
