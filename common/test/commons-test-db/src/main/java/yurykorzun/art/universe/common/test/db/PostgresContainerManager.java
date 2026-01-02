package yurykorzun.art.universe.common.test.db;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class PostgresContainerManager {

    private static final String IMAGE = "postgres:14-alpine";
    private static final Map<String, PostgreSQLContainer<?>> containers = new ConcurrentHashMap<>();

    public static PostgreSQLContainer<?> get(PostgresTestContainer cfg) {
        String key = key(cfg);
        return containers.computeIfAbsent(key, k -> start(cfg));
    }

    private static PostgreSQLContainer<?> start(PostgresTestContainer cfg) {
        log.info("Starting PostgreSQL container: {}", key(cfg));

        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(IMAGE)
            .withDatabaseName(cfg.databaseName())
            .withUsername(cfg.username())
            .withPassword(cfg.password())
            .withReuse(true);

        if (!cfg.initScript().isEmpty()) {
            container.withInitScript(cfg.initScript());
        }

        container.start();
        return container;
    }

    private static String key(PostgresTestContainer cfg) {
        return cfg.databaseName() + ":" + cfg.username() + ":" + cfg.initScript();
    }

    private PostgresContainerManager() {}
}