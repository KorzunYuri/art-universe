package yurykorzun.art.universe.common.observability.util;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

@ConditionalOnBean(ObservationRegistry.class)
@Component
@Profile("!test")
public class ObservabilityServiceImpl implements ObservabilityService {

    private final ObservationRegistry registry;

    public ObservabilityServiceImpl(ObservationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void observe(String name, Runnable action) {
        createNotStarted(name, o -> {}).observe(action);
    }

    @Override
    public void observe(String name, Runnable action, Consumer<Observation> customizer) {
        createNotStarted(name, customizer).observe(action);
    }

    @Override
    public <T> T observe(String name, Supplier<T> action) {
        return createNotStarted(name, o -> {}).observe(action);
    }

    @Override
    public <T> T observe(String name, Supplier<T> action, Consumer<Observation> customizer) {
        return createNotStarted(name, customizer).observe(action);
    }

    private Observation createNotStarted(String name, Consumer<Observation> customizer) {
        Observation observation = Observation.createNotStarted(name, registry);
        customizer.accept(observation);
        return observation;
    }
}
