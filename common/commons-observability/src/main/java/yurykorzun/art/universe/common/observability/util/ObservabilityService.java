package yurykorzun.art.universe.common.observability.util;

import io.micrometer.observation.Observation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ObservabilityService {

    void observe(String name, Runnable action);
    void observe(String name, Runnable action, Consumer<Observation> customizer);
    <T> T observe(String name, Supplier<T> action);
    <T> T observe(String name, Supplier<T> action, Consumer<Observation> customizer);
}
