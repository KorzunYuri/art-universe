package yurykorzun.art.universe.common.config.client;

import yurykorzun.art.universe.common.config.client.dto.PropertyResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory store for dynamic property values fetched from the config service.
 * <p>
 * Populated on startup by {@link ConfigPropertyAutoRegistrator} and refreshed periodically
 * on a background thread. Consumer modules inject this bean and call the typed getters.
 */
public class ConfigPropertyHolder {

    private final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();

    /**
     * Replaces the in-memory values with the latest data from the config service.
     * Thread-safe: individual key updates are atomic via ConcurrentHashMap.
     */
    public void updateAll(List<PropertyResponse> properties) {
        properties.forEach(p -> {
            Object parsed = p.getPropertyType().parse(p.getCurrentValue());
            values.put(p.getKey(), parsed);
        });
    }

    public int getInt(ConfigurableProperty property) {
        return (Integer) getValue(property);
    }

    public boolean getBoolean(ConfigurableProperty property) {
        return (Boolean) getValue(property);
    }

    public BigDecimal getDecimal(ConfigurableProperty property) {
        return (BigDecimal) getValue(property);
    }

    public String getString(ConfigurableProperty property) {
        return (String) getValue(property);
    }

    private Object getValue(ConfigurableProperty property) {
        Object value = values.get(property.getKey());
        if (value == null) {
            throw new IllegalStateException(
                "Property '" + property.getKey() + "' is not loaded. " +
                "Ensure the config service is reachable and au.config.client.service-url is configured."
            );
        }
        return value;
    }

    /**
     * Returns {@code true} if the given property has been loaded into the holder.
     * Useful for conditional logic during startup.
     */
    public boolean isLoaded(ConfigurableProperty property) {
        return values.containsKey(property.getKey());
    }

    /**
     * Returns the number of properties currently held in memory.
     */
    public int size() {
        return values.size();
    }
}
