package yurykorzun.art.universe.music.data.raw.spotify.etl.messaging;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.util.Arrays;
import java.util.List;

public final class SpotifyKafkaTopics {

    public static final String CALLS_PREFIX = "spotify.calls.";
    public static final String RESPONSES_TOPIC = "spotify.responses";

    private SpotifyKafkaTopics() {}

    /**
     * Derives the Kafka topic name for a given API call type.
     * Example: ARTIST_GET (method="artist.get") → "spotify.calls.artist-get"
     */
    public static String callTopicFor(SpotifyApiCallType type) {
        return CALLS_PREFIX + type.getMethod().replace('.', '-');
    }
}
