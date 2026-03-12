package yurykorzun.art.universe.common.test.db;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class PostgresContainerManager {

    private static final String IMAGE = "postgres:14-alpine";

    private static final Map<String, PostgreSQLContainer<?>> containers = new ConcurrentHashMap<>();
    /** Tracks which initScripts have already been applied per container (keyed by databaseName). */
    private static final Map<String, Set<String>> appliedScripts = new ConcurrentHashMap<>();

    public static PostgreSQLContainer<?> get(PostgresTestContainer cfg) {
        String key = cfg.databaseName();
        PostgreSQLContainer<?> container = containers.computeIfAbsent(key, k -> start(cfg));

        if (!cfg.initScript().isEmpty()) {
            Set<String> applied = appliedScripts.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
            if (applied.add(cfg.initScript())) {
                runInitScript(container, cfg.databaseName(), cfg.initScript());
            }
        }

        return container;
    }

    private static PostgreSQLContainer<?> start(PostgresTestContainer cfg) {
        log.info("Starting PostgreSQL container for database: {}", cfg.databaseName());

        Path initdbPath = resolveInitdbPath();

        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(IMAGE)
            .withDatabaseName(cfg.databaseName())
            .withCopyToContainer(
                MountableFile.forHostPath(initdbPath.resolve("01-init.sh")),
                "/docker-entrypoint-initdb.d/01-init.sh")
            .withCopyToContainer(
                MountableFile.forHostPath(initdbPath.resolve("changesets")),
                "/docker-entrypoint-initdb.d/changesets/")
            .withReuse(true);

        container.start();
        return container;
    }

    private static void runInitScript(PostgreSQLContainer<?> container, String databaseName, String initScript) {
        log.info("Running init script '{}' against database '{}'", initScript, databaseName);

        byte[] scriptBytes;
        try (InputStream is = PostgresContainerManager.class.getClassLoader().getResourceAsStream(initScript)) {
            if (is == null) {
                throw new IllegalArgumentException("Init script not found on classpath: " + initScript);
            }
            scriptBytes = is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read init script: " + initScript, e);
        }

        container.copyFileToContainer(Transferable.of(scriptBytes), "/tmp/test-module-init.sql");

        try {
            ExecResult result = container.execInContainer(
                "psql", "-v", "ON_ERROR_STOP=1",
                "-U", container.getUsername(),
                "-d", databaseName,
                "-f", "/tmp/test-module-init.sql");

            if (result.getExitCode() != 0) {
                throw new IllegalStateException(
                    "Init script '" + initScript + "' failed (exit " + result.getExitCode() + "):\n" + result.getStderr());
            }
            log.info("Init script '{}' applied successfully", initScript);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to execute init script: " + initScript, e);
        }
    }

    private static Path resolveInitdbPath() {
        String projectRoot = System.getProperty("project.root");
        if (projectRoot == null) {
            Path cwd = Paths.get("").toAbsolutePath();
            for (Path p = cwd; p != null; p = p.getParent()) {
                Path candidate = p.resolve("env/docker/common/db/initdb");
                if (candidate.toFile().isDirectory()) {
                    log.warn("project.root system property not set — resolved initdb path via filesystem walk: {}", candidate);
                    return candidate;
                }
            }
            throw new IllegalStateException(
                "Cannot locate env/docker/common/db/initdb. Set -Dproject.root=<project-root> in the test task.");
        }
        return Paths.get(projectRoot, "env", "docker", "common", "db", "initdb");
    }

    private PostgresContainerManager() {}
}
