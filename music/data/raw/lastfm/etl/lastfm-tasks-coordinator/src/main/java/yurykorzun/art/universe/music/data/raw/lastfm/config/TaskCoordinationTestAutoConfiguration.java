package yurykorzun.art.universe.music.data.raw.lastfm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.CoordinationStateProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider.InMemoryCoordinationProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Auto-configuration for task coordination test beans.
 * <p>
 * Provides in-memory coordination provider suitable for testing.
 * Automatically activated for @SpringBootTest via META-INF/spring/org.springframework.boot.test.autoconfigure.AutoConfiguration.imports
 * <p>
 * For slice tests (@DataJpaTest, etc.), this must be explicitly imported via @Import.
 */
@AutoConfiguration
@Slf4j
public class TaskCoordinationTestAutoConfiguration {

    /**
     * In-memory coordination provider for tests.
     * Single-instance, no database required.
     */
    @Bean
    @ConditionalOnMissingBean
    public CoordinationStateProvider coordinationStateProvider() {
        log.debug("Configuring IN-MEMORY coordination provider for tests");
        return new InMemoryCoordinationProvider();
    }

    /**
     * Instance ID for test coordinator.
     */
    @Bean
    @ConditionalOnMissingBean(name = "coordinationInstanceId")
    public String coordinationInstanceId() {
        return "test-instance";
    }

    /**
     * Executor service for test coordinator.
     */
    @Bean(name = "tasksExecutor")
    @ConditionalOnMissingBean(name = "tasksExecutor")
    public ExecutorService tasksExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("test-tasks-" + t.threadId());
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Task coordinator for tests.
     */
    @Bean
    @ConditionalOnMissingBean
    public TaskCoordinator taskCoordinator(
            CoordinationStateProvider stateProvider,
            String coordinationInstanceId,
            ExecutorService tasksExecutor
    ) {
        log.debug("Configuring TaskCoordinator for tests");
        return new TaskCoordinator(stateProvider, coordinationInstanceId, tasksExecutor);
    }
}
