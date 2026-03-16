package yurykorzun.art.universe.music.data.raw.spotify.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SpotifyPerformerProperty implements ConfigurableProperty {

    SCHEDULE_DELAY_SECS(
        "spotify.performer.schedule.delay-secs",
        PropertyType.INTEGER, "1",
        "Delay in seconds between API call execution runs",
        PropertyConstraints.ofRange(1, 60)
    ),
    RATE_LIMITER_MIN_DELAY_MS(
        "spotify.performer.rate-limiter.min-delay-ms",
        PropertyType.INTEGER, "1000",
        "Minimum delay between Spotify API calls in milliseconds",
        PropertyConstraints.ofRange(50, 5000)
    ),
    RATE_LIMITER_MAX_DELAY_MS(
        "spotify.performer.rate-limiter.max-delay-ms",
        PropertyType.INTEGER, "30000",
        "Maximum delay between Spotify API calls when rate-limited (ms)",
        PropertyConstraints.ofRange(1000, 300000)
    ),
    RATE_LIMITER_BACKOFF_MULTIPLIER(
        "spotify.performer.rate-limiter.backoff-multiplier",
        PropertyType.DECIMAL, "2.0",
        "Backoff multiplier applied on 429 response",
        PropertyConstraints.ofRange(1.0, 10.0)
    ),
    RETRY_MAX_ATTEMPTS(
        "spotify.performer.retry.max-attempts",
        PropertyType.INTEGER, "3",
        "Maximum retry attempts for failed API calls",
        PropertyConstraints.ofRange(1, 10)
    ),

    // Kafka weighted quota properties
    QUOTA_WEIGHT_ARTIST_GET(
        "spotify.performer.quota.weight.artist-get",
        PropertyType.INTEGER, "30",
        "Quota weight for ARTIST_GET calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_ARTIST_ALBUMS(
        "spotify.performer.quota.weight.artist-albums",
        PropertyType.INTEGER, "20",
        "Quota weight for ARTIST_ALBUMS calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_ALBUM_GET(
        "spotify.performer.quota.weight.album-get",
        PropertyType.INTEGER, "15",
        "Quota weight for ALBUM_GET calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_ALBUM_TRACKS(
        "spotify.performer.quota.weight.album-tracks",
        PropertyType.INTEGER, "5",
        "Quota weight for ALBUM_TRACKS calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_TRACK_GET(
        "spotify.performer.quota.weight.track-get",
        PropertyType.INTEGER, "10",
        "Quota weight for TRACK_GET calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_SEARCH_ARTIST(
        "spotify.performer.quota.weight.search-artist",
        PropertyType.INTEGER, "15",
        "Quota weight for SEARCH_ARTIST calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_SEARCH_ALBUM(
        "spotify.performer.quota.weight.search-album",
        PropertyType.INTEGER, "3",
        "Quota weight for SEARCH_ALBUM calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    QUOTA_WEIGHT_SEARCH_TRACK(
        "spotify.performer.quota.weight.search-track",
        PropertyType.INTEGER, "2",
        "Quota weight for SEARCH_TRACK calls",
        PropertyConstraints.ofRange(0, 100)
    ),
    KAFKA_BATCH_PER_TYPE(
        "spotify.performer.kafka.batch-per-type",
        PropertyType.INTEGER, "5",
        "Maximum messages consumed per call type per cycle",
        PropertyConstraints.ofRange(1, 50)
    ),
    KAFKA_RETRY_SWEEP_CYCLES(
        "spotify.performer.kafka.retry-sweep-cycles",
        PropertyType.INTEGER, "10",
        "Number of cycles between DUE_TO_RETRY DB sweeps",
        PropertyConstraints.ofRange(1, 100)
    ),
    KAFKA_ORPHAN_SWEEP_CYCLES(
        "spotify.performer.kafka.orphan-sweep-cycles",
        PropertyType.INTEGER, "30",
        "Number of cycles between orphaned CREATED calls DB sweeps",
        PropertyConstraints.ofRange(1, 200)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SpotifyPerformerProperty(String key, PropertyType propertyType, String defaultValue,
                             String description, PropertyConstraints constraints) {
        this.key = key;
        this.propertyType = propertyType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.constraints = constraints;
    }

    @Override public String getKey() { return key; }
    @Override public PropertyType getPropertyType() { return propertyType; }
    @Override public String getDefaultValue() { return defaultValue; }
    @Override public String getDescription() { return description; }
    @Override public PropertyConstraints getConstraints() { return constraints; }
}
