package yurykorzun.art.universe.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Registry<K, V> {

    private final Map<K, V> registry = new ConcurrentHashMap<>();

    private Registry() {}

    private static final Registry<?, ?> INSTANCE = new Registry<>();

    @SuppressWarnings("unchecked")
    public static <K, V> Registry<K, V> getInstance() {
        return (Registry<K, V>) INSTANCE;
    }

    public void register(K key, V value) {
        registry.put(key, value);
    }

    public V get(K key) {
        return registry.get(key);
    }
}

