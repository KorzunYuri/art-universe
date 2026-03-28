package yurykorzun.art.universe.music.data.semantic.parser.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SemanticParserProperty implements ConfigurableProperty {

    POLLING_FALLBACK_DELAY_SECS(
        "semantic.parser.polling.fallback-delay-secs",
        PropertyType.INTEGER, "5",
        "Fallback polling delay in seconds when no NOTIFY received",
        PropertyConstraints.ofRange(1, 300)
    ),

    BATCH_SIZE(
        "semantic.parser.batch-size",
        PropertyType.INTEGER, "10",
        "Maximum number of completed requests to process per batch",
        PropertyConstraints.ofRange(1, 100)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SemanticParserProperty(String key, PropertyType propertyType, String defaultValue,
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
