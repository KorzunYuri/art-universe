package yurykorzun.art.universe.common.config.client;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.common.config.client.dto.PropertyResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-safe in-memory store for dynamic property values fetched from the config service.
 * <p>
 * Populated on startup by {@link ConfigPropertyAutoRegistrator} and refreshed periodically
 * on a background thread. Consumer modules inject this bean and call the typed getters.
 * <p>
 * Supports change listeners: register a callback via {@link #onChange(ConfigurableProperty, Consumer)}
 * to be notified when a property value changes during a refresh cycle. Listeners receive the
 * new parsed value and are invoked synchronously on the refresh thread.
 */
@Slf4j
public class ConfigPropertyHolder {

    private final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    /**
     * Replaces the in-memory values with the latest data from the config service.
     * Thread-safe: individual key updates are atomic via ConcurrentHashMap.
     * Fires change listeners for any property whose value differs from the previous one.
     */
    public void updateAll(List<PropertyResponse> properties) {
        properties.forEach(p -> {
            Object parsed = p.getPropertyType().parse(p.getCurrentValue());
            Object previous = values.put(p.getKey(), parsed);
            if (previous != null && !Objects.equals(previous, parsed)) {
                fireListeners(p.getKey(), parsed);
            }
        });
    }

    /**
     * Registers a listener that is called whenever the given property's value changes
     * during a refresh cycle. The consumer receives the new parsed value.
     * <p>
     * Listeners are invoked synchronously on the refresh thread. Keep them lightweight
     * (e.g. setting a volatile field on the target object).
     */
    public void onChange(ConfigurableProperty property, Consumer<Object> listener) {
        listeners.computeIfAbsent(property.getKey(), k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    private void fireListeners(String key, Object newValue) {
        List<Consumer<Object>> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            for (Consumer<Object> listener : keyListeners) {
                try {
                    listener.accept(newValue);
                } catch (Exception e) {
                    log.warn("Config change listener failed for key '{}': {}", key, e.getMessage());
                }
            }
        }
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
