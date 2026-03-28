package yurykorzun.art.universe.music.data.semantic.applicator.config;

import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.config.client.PropertyConstraints;
import yurykorzun.art.universe.common.config.client.PropertyType;

public enum SemanticApplicatorProperty implements ConfigurableProperty {

    POLLING_FALLBACK_DELAY_SECS(
        "semantic.applicator.polling.fallback-delay-secs",
        PropertyType.INTEGER, "15",
        "Fallback polling delay in seconds when no NOTIFY received",
        PropertyConstraints.ofRange(1, 300)
    ),

    BATCH_SIZE(
        "semantic.applicator.batch-size",
        PropertyType.INTEGER, "20",
        "Maximum number of proposals to process per batch",
        PropertyConstraints.ofRange(1, 500)
    ),

    AUTO_APPROVE_THRESHOLD(
        "semantic.applicator.threshold.auto-approve",
        PropertyType.INTEGER, "90",
        "Minimum confidence for auto-approval",
        PropertyConstraints.ofRange(50, 100)
    ),

    AUTO_DECLINE_THRESHOLD(
        "semantic.applicator.threshold.auto-decline",
        PropertyType.INTEGER, "20",
        "Maximum confidence for auto-decline",
        PropertyConstraints.ofRange(0, 50)
    );

    private final String key;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String description;
    private final PropertyConstraints constraints;

    SemanticApplicatorProperty(String key, PropertyType propertyType, String defaultValue,
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
