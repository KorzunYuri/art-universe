package yurykorzun.art.universe.music.data.raw.spotify.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SpotifyCommonProperty implements ConfigurableProperty {

    KAFKA_ENABLED(
        "spotify.kafka.enabled",
        PropertyType.BOOLEAN, "true",
        "Enable Kafka-based dispatch for all Spotify ETL modules (false = DB polling fallback)",
        null
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SpotifyCommonProperty(String key, PropertyType propertyType, String defaultValue,
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
