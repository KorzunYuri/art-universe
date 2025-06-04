package yurykorzun.art.universe.common;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class CodedRegistry {

    private static final Map<Class<? extends Coded>, Map<Integer, ? extends Coded>> REGISTRIES = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Coded> void register(T instance, Class<T> clazz) {
        Map<Integer, T> registry = (Map<Integer, T>) REGISTRIES.computeIfAbsent(clazz,
                k -> new ConcurrentHashMap<Integer, T>());
        registry.put(instance.getCode(), instance);
    }

    public static <T extends Coded> void register(Collection<T> instances, Class<T> clazz) {
        for (T instance : instances) {
            register(instance, clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Coded> Optional<T> getByCode(int code, Class<T> clazz) {
        Map<Integer, T> registry = (Map<Integer, T>) REGISTRIES.get(clazz);
        if (registry == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registry.get(code));
    }

    /**
     * Returns all registered 'domain' classes.
     */
    public static Set<Class<? extends Coded>> getDomains() {
        return REGISTRIES.keySet();
    }

    /**
     * Returns all registered {@link Coded} instances for a specific domain.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Coded> Map<Integer, T> getRegistry(Class<T> clazz) {
        return (Map<Integer, T>) REGISTRIES.getOrDefault(clazz, new ConcurrentHashMap<>());
    }
}
