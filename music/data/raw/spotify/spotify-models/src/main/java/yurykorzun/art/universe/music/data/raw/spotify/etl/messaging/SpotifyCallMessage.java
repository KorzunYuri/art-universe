package yurykorzun.art.universe.music.data.raw.spotify.etl.messaging;

import java.time.Instant;

/**
 * Kafka message sent from Generator to Performer.
 * Contains the api_call ID and enough metadata for the performer
 * to fetch and execute the call.
 */
public record SpotifyCallMessage(
        long apiCallId,
        int typeCode,
        String spotifyId,
        Integer entityTypeCode,
        Long entityId,
        Instant createdAt
) {}
