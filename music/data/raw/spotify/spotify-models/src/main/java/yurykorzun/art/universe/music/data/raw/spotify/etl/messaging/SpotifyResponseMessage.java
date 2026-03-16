package yurykorzun.art.universe.music.data.raw.spotify.etl.messaging;

import java.time.Instant;

/**
 * Kafka message sent from Performer to Parser.
 * Notifies the parser that a new API response is ready for processing.
 */
public record SpotifyResponseMessage(
        long apiResponseId,
        long apiCallId,
        int callTypeCode,
        Instant createdAt
) {}
