package yurykorzun.art.universe.music.data.master.category.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum MasterTriggerProperty implements ConfigurableProperty {

    SCAN_INTERVAL_SECS(
        "master.trigger.scan-interval-secs",
        PropertyType.INTEGER, "3600",
        "Interval in seconds between master entity analysis trigger scans",
        PropertyConstraints.ofRange(60, 86400)
    ),

    BATCH_SIZE(
        "master.trigger.batch-size",
        PropertyType.INTEGER, "100",
        "Maximum number of entities per scan batch",
        PropertyConstraints.ofRange(1, 5000)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    MasterTriggerProperty(String key, PropertyType propertyType, String defaultValue,
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
