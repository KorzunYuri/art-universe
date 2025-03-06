package yurykorzun.art.universe.common.data.raw.task.entity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to be used by other modules to 'register' their task types.
 * This will help to process the tasks in a unified way.
 */
public class TaskTypeRegistry {

    private TaskTypeRegistry() {
        // no instance
    }

    private static final Map<String, TaskType> REGISTRY = new ConcurrentHashMap<>();

    public static void register(TaskType type) {
        REGISTRY.put(type.getCode(), type);
    }

    public static Optional<TaskType> getByCode(String code) {
        return Optional.ofNullable(REGISTRY.get(code));
    }

    public static Map<String, TaskType> getRegistry() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
