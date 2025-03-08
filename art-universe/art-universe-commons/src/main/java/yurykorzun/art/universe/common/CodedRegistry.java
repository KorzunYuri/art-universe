package yurykorzun.art.universe.common;


import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class CodedRegistry<T extends Coded> {

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
}
