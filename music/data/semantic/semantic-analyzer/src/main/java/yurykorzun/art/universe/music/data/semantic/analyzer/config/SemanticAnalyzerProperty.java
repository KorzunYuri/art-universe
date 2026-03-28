package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SemanticAnalyzerProperty implements ConfigurableProperty {

    POLLING_FALLBACK_DELAY_SECS(
        "semantic.analyzer.polling.fallback-delay-secs",
        PropertyType.INTEGER, "10",
        "Fallback polling delay in seconds when no NOTIFY received",
        PropertyConstraints.ofRange(1, 300)
    ),

    BATCH_SIZE(
        "semantic.analyzer.batch-size",
        PropertyType.INTEGER, "5",
        "Maximum number of tickets to process per batch",
        PropertyConstraints.ofRange(1, 100)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SemanticAnalyzerProperty(String key, PropertyType propertyType, String defaultValue,
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
