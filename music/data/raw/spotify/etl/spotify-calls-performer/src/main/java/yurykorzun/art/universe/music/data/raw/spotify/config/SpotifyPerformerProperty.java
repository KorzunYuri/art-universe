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
