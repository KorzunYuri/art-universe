package yurykorzun.art.universe.common.config.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import yurykorzun.art.universe.common.config.client.dto.PropertyResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Discovers all {@link ConfigurableProperty} enum implementations on the classpath,
 * registers them with the config service on startup, and schedules periodic refresh.
 * <p>
 * Owns the {@link ConfigPropertyHolder} it populates. Consumers should obtain the holder
 * via {@link #getHolder()} — wired through {@code CommonsConfigClientAutoConfiguration} —
 * which guarantees the holder is fully populated before any consumer bean receives it.
 */
@Slf4j
public class ConfigPropertyAutoRegistrator {

    private final ConfigServiceClient client;
    private final ConfigClientProperties properties;
    @Getter
    private final ConfigPropertyHolder holder;
    private final ScheduledExecutorService scheduler;

    public ConfigPropertyAutoRegistrator(ConfigServiceClient client, ConfigClientProperties properties) {
        this.client = client;
        this.properties = properties;
        this.holder = new ConfigPropertyHolder();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "config-refresh-thread");
            t.setDaemon(true);
            return t;
        });
    }

    @PostConstruct
    public void initialize() {
        List<ConfigurableProperty> discovered = discoverAllProperties();
        if (discovered.isEmpty()) {
            log.info("No ConfigurableProperty enums found on classpath, skipping config registration");
            return;
        }
        log.info("Discovered {} configurable properties, registering with config service at {}",
            discovered.size(), properties.getServiceUrl());

        List<PropertyResponse> current = client.registerAll(discovered);
        holder.updateAll(current);
        log.info("Config property holder populated with {} properties", holder.size());

        long intervalSecs = properties.getRefreshIntervalSeconds();
        scheduler.scheduleWithFixedDelay(this::refreshValues, intervalSecs, intervalSecs, TimeUnit.SECONDS);
        log.info("Config refresh scheduled every {} seconds", intervalSecs);
    }

    private void refreshValues() {
        try {
            List<PropertyResponse> current = client.fetchAll();
            holder.updateAll(current);
            log.debug("Config properties refreshed ({} total)", current.size());
        } catch (Exception e) {
            log.warn("Config refresh failed, retaining previous values: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<ConfigurableProperty> discoverAllProperties() {
        Reflections reflections = new Reflections("yurykorzun.art.universe");
        Set<Class<? extends ConfigurableProperty>> classes = reflections.getSubTypesOf(ConfigurableProperty.class);
        return classes.stream()
            .filter(Class::isEnum)
            .flatMap(clazz -> Arrays.stream((ConfigurableProperty[]) clazz.getEnumConstants()))
            .collect(Collectors.toList());
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
