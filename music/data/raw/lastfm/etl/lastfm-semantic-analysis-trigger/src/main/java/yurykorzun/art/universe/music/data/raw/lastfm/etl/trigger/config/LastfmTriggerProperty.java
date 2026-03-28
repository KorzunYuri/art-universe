package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum LastfmTriggerProperty implements ConfigurableProperty {

    SCAN_INTERVAL_SECS(
        "lastfm.trigger.scan-interval-secs",
        PropertyType.INTEGER, "3600",
        "Interval in seconds between analysis trigger scans",
        PropertyConstraints.ofRange(60, 86400)
    ),

    ARTIST_MIN_LISTENERS(
        "lastfm.trigger.artist.min-listeners",
        PropertyType.INTEGER, "1000",
        "Minimum listener count for artist eligibility",
        PropertyConstraints.ofRange(0, 10000000)
    ),
    ARTIST_BATCH_SIZE(
        "lastfm.trigger.artist.batch-size",
        PropertyType.INTEGER, "50",
        "Maximum number of artists per scan batch",
        PropertyConstraints.ofRange(1, 1000)
    ),

    ALBUM_MIN_LISTENERS(
        "lastfm.trigger.album.min-listeners",
        PropertyType.INTEGER, "1000",
        "Minimum listener count for album eligibility",
        PropertyConstraints.ofRange(0, 10000000)
    ),
    ALBUM_BATCH_SIZE(
        "lastfm.trigger.album.batch-size",
        PropertyType.INTEGER, "50",
        "Maximum number of albums per scan batch",
        PropertyConstraints.ofRange(1, 1000)
    ),

    TRACK_MIN_LISTENERS(
        "lastfm.trigger.track.min-listeners",
        PropertyType.INTEGER, "1000",
        "Minimum listener count for track eligibility",
        PropertyConstraints.ofRange(0, 10000000)
    ),
    TRACK_BATCH_SIZE(
        "lastfm.trigger.track.batch-size",
        PropertyType.INTEGER, "50",
        "Maximum number of tracks per scan batch",
        PropertyConstraints.ofRange(1, 1000)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    LastfmTriggerProperty(String key, PropertyType propertyType, String defaultValue,
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
