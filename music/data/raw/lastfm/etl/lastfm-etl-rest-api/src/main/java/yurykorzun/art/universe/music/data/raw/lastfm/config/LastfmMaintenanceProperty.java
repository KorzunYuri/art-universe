package yurykorzun.art.universe.music.data.raw.lastfm.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum LastfmMaintenanceProperty implements ConfigurableProperty {

    THRESHOLD_ARTIST_LISTENERS_COUNT(
        "lastfm.maintenance.threshold.artist.listeners-count",
        PropertyType.INTEGER, "1000",
        "Minimum listeners count; artists below this are candidates for cleanup",
        PropertyConstraints.ofRange(0, 10000000)
    ),
    THRESHOLD_ALBUM_PLAY_COUNT(
        "lastfm.maintenance.threshold.album.play-count",
        PropertyType.INTEGER, "10000",
        "Minimum play count; albums below this are candidates for cleanup",
        PropertyConstraints.ofRange(0, 100000000)
    ),
    THRESHOLD_TRACK_PLAY_COUNT(
        "lastfm.maintenance.threshold.track.play-count",
        PropertyType.INTEGER, "1000",
        "Minimum play count; tracks below this are candidates for cleanup",
        PropertyConstraints.ofRange(0, 100000000)
    ),
    THRESHOLD_TAG_USAGE_COUNT(
        "lastfm.maintenance.threshold.tag.usage-count",
        PropertyType.INTEGER, "1000",
        "Minimum usage count; tags below this are candidates for cleanup",
        PropertyConstraints.ofRange(0, 10000000)
    ),
    UNBIND_BATCH_SIZE(
        "lastfm.maintenance.unbind.batch-size",
        PropertyType.INTEGER, "1000",
        "Batch size for unbinding deleted entities from master data",
        PropertyConstraints.ofRange(10, 10000)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    LastfmMaintenanceProperty(String key, PropertyType propertyType, String defaultValue,
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
