package yurykorzun.art.universe.music.data.raw.spotify.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SpotifyApplicatorProperty implements ConfigurableProperty {

    STAGING_APPLY_DELAY_SECS(
        "spotify.applicator.staging-apply.delay-secs",
        PropertyType.INTEGER, "10",
        "Delay in seconds between staging application runs",
        PropertyConstraints.ofRange(1, 3600)
    ),
    STAGING_CLEANUP_DELAY_SECS(
        "spotify.applicator.staging-cleanup.delay-secs",
        PropertyType.INTEGER, "3600",
        "Delay in seconds between staging cleanup runs",
        PropertyConstraints.ofRange(60, 86400)
    ),
    STAGING_CLEANUP_RETENTION_HOURS(
        "spotify.applicator.staging.cleanup.retention-hours",
        PropertyType.INTEGER, "240",
        "Hours to retain completed staging iterations before cleanup",
        PropertyConstraints.ofRange(1, 8760)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SpotifyApplicatorProperty(String key, PropertyType propertyType, String defaultValue,
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
