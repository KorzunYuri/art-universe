package yurykorzun.art.universe.common.config.client;

/**
 * Marker interface for enums that declare dynamic configurable properties.
 * Each enum constant represents one property managed by the centralized config service.
 * <p>
 * Consumer usage:
 * <pre>
 *   {@literal @}Getter
 *   {@literal @}RequiredArgsConstructor
 *   public enum MyModuleConfig implements ConfigurableProperty {
 *       SOME_DELAY_SECS(
 *           "mymodule.scheduler.delay-secs",
 *           PropertyType.INTEGER,
 *           "30",
 *           "Delay in seconds between runs",
 *           PropertyConstraints.ofRange(1, 3600)
 *       );
 *
 *       private final String key;
 *       private final PropertyType propertyType;
 *       private final String defaultValue;
 *       private final String description;
 *       private final PropertyConstraints constraints;
 *   }
 * </pre>
 * The {@code commons-config-client} auto-configuration discovers all enums implementing
 * this interface on the classpath, registers them with the config service on startup,
 * and refreshes their values periodically.
 */
public interface ConfigurableProperty {

    String getKey();

    PropertyType getPropertyType();

    String getDefaultValue();

    String getDescription();

    /**
     * Returns the constraints for this property, or {@code null} if unconstrained.
     */
    PropertyConstraints getConstraints();
}
