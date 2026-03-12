package yurykorzun.art.universe.music.data.raw.spotify.common;

import com.google.common.hash.Hashing;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

import java.nio.charset.StandardCharsets;

public class SyntheticIdGenerator {

    private SyntheticIdGenerator() {}

    public static long generate(SpotifyEntityType entityType, String spotifyId) {
        String key = entityType.getCode() + ":" + spotifyId;
        long hash = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asLong();
        return -(Math.abs(hash) + 1);
    }
}
