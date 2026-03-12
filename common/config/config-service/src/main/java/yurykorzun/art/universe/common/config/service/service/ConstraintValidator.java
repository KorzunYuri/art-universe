package yurykorzun.art.universe.common.config.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.PropertyType;
import yurykorzun.art.universe.common.config.service.entity.ConfigPropertyEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConstraintValidator {

    private final ObjectMapper objectMapper;

    /**
     * Validates {@code newValue} against the type and constraints of the given entity.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate(ConfigPropertyEntity entity, String newValue) {
        PropertyType type = entity.getPropertyType();

        // Step 1: Validate parsability for the declared type
        Object parsed;
        try {
            parsed = type.parse(newValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Value '" + newValue + "' cannot be parsed as " + type + ": " + e.getMessage()
            );
        }

        // Step 2: Validate against constraints_json (if present)
        if (entity.getConstraintsJson() == null || entity.getConstraintsJson().isBlank()) {
            return;
        }

        try {
            JsonNode constraints = objectMapper.readTree(entity.getConstraintsJson());
            switch (type) {
                case INTEGER, DECIMAL -> validateNumericRange(newValue, parsed, constraints);
                case STRING -> validateAllowedValues(newValue, constraints);
                case BOOLEAN -> { /* parsability check is sufficient */ }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not parse constraints_json for property '{}', skipping constraint check: {}",
                entity.getKey(), e.getMessage());
        }
    }

    private void validateNumericRange(String rawValue, Object parsed, JsonNode constraints) {
        BigDecimal value = new BigDecimal(rawValue);

        if (constraints.has("min")) {
            BigDecimal min = constraints.get("min").decimalValue();
            if (value.compareTo(min) < 0) {
                throw new IllegalArgumentException(
                    "Value " + rawValue + " is below the minimum allowed value of " + min
                );
            }
        }
        if (constraints.has("max")) {
            BigDecimal max = constraints.get("max").decimalValue();
            if (value.compareTo(max) > 0) {
                throw new IllegalArgumentException(
                    "Value " + rawValue + " exceeds the maximum allowed value of " + max
                );
            }
        }
    }

    private void validateAllowedValues(String value, JsonNode constraints) {
        if (!constraints.has("allowedValues")) {
            return;
        }
        List<String> allowed = new ArrayList<>();
        constraints.get("allowedValues").forEach(node -> allowed.add(node.asText()));
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException(
                "Value '" + value + "' is not in the list of allowed values: " + allowed
            );
        }
    }
}
