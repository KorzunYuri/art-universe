package yurykorzun.art.universe.music.data.raw.lastfm.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum LastfmPerformerProperty implements ConfigurableProperty {

    SCHEDULE_DELAY_SECS(
        "lastfm.performer.schedule.delay-secs",
        PropertyType.INTEGER, "1",
        "Delay in seconds between API call execution runs",
        PropertyConstraints.ofRange(1, 60)
    ),
    CALLS_PER_SEC(
        "lastfm.performer.calls-per-sec",
        PropertyType.DECIMAL, "1.0",
        "Maximum LastFM API calls per second (rate limiter)",
        PropertyConstraints.ofRange(0.1, 5.0)
    ),
    RETRY_MAX_ATTEMPTS(
        "lastfm.performer.retry.max-attempts",
        PropertyType.INTEGER, "3",
        "Maximum retry attempts for failed API calls",
        PropertyConstraints.ofRange(1, 10)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    LastfmPerformerProperty(String key, PropertyType propertyType, String defaultValue,
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
