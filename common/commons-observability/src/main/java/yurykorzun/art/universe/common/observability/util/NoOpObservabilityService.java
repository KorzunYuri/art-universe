package yurykorzun.art.universe.common.observability.util;

import io.micrometer.observation.Observation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>No-op version of {@link ObservabilityService} that simply executes provided actions, designed for testing purposes.</p>
 * <p>Can be used both in @SpringBootTest and unit-test (via {@link NoOpObservabilityService#getInstance()}</p>
 */
@ConditionalOnMissingBean(ObservabilityService.class)
@Component
@Profile("test")
public class NoOpObservabilityService implements ObservabilityService {

    @Override
    public void observe(String name, Runnable action) {
        action.run();
    }

    @Override
    public void observe(String name, Runnable action, Consumer<Observation> customizer) {
        action.run();
    }

    @Override
    public <T> T observe(String name, Supplier<T> action) {
        return action.get();
    }

    @Override
    public <T> T observe(String name, Supplier<T> action, Consumer<Observation> customizer) {
        return action.get();
    }

    public static ObservabilityService getInstance() {
        return new NoOpObservabilityService();
    }
}
