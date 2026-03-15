package yurykorzun.art.universe.common.config.client;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Value object describing validation constraints for a {@link ConfigurableProperty}.
 * <ul>
 *   <li>{@code min} / {@code max} — numeric range for {@link PropertyType#INTEGER} and
 *       {@link PropertyType#DECIMAL} properties</li>
 *   <li>{@code allowedValues} — exhaustive list of permitted values for
 *       {@link PropertyType#STRING} properties</li>
 * </ul>
 * All fields are optional; pass {@code null} for unconstrained properties.
 */
@Getter
public class PropertyConstraints {

    private final BigDecimal min;
    private final BigDecimal max;
    private final List<String> allowedValues;

    private PropertyConstraints(BigDecimal min, BigDecimal max, List<String> allowedValues) {
        this.min = min;
        this.max = max;
        this.allowedValues = allowedValues;
    }

    public static PropertyConstraints ofRange(Number min, Number max) {
        return new PropertyConstraints(
            new BigDecimal(min.toString()),
            new BigDecimal(max.toString()),
            null
        );
    }

    public static PropertyConstraints ofAllowedValues(String... values) {
        return new PropertyConstraints(null, null, Arrays.asList(values));
    }
}
