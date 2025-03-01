package yurykorzun.art.universe.common.persistence.entity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to be used by other modules to 'register' their task types.
 * This will help to process the tasks in a unified way.
 */
public class DataCollectionTaskTypeRegistry {

    private DataCollectionTaskTypeRegistry() {
        // no instance
    }

    private static final Map<String, DataCollectionTaskType> REGISTRY = new ConcurrentHashMap<>();

    public static void register(DataCollectionTaskType type) {
        REGISTRY.put(type.getCode(), type);
    }

    public static Optional<DataCollectionTaskType> getByCode(String code) {
        return Optional.ofNullable(REGISTRY.get(code));
    }
}
