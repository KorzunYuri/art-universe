package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ApiCallTypeRegistry {

    private ApiCallTypeRegistry() {
        // no instance
    }

    private static final Map<String, ApiCallType> REGISTRY = new ConcurrentHashMap<>();

    public static void register(ApiCallType type) {
        REGISTRY.put(type.getMethod(), type);
    }

    public static Optional<ApiCallType> getByCode(String code) {
        return Optional.ofNullable(REGISTRY.get(code));
    }

    public static Map<String, ApiCallType> getRegistry() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
