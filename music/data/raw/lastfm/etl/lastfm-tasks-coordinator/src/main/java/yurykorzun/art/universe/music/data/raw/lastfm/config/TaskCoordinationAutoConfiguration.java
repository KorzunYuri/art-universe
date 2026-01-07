package yurykorzun.art.universe.music.data.raw.lastfm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationInstanceRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationRunningTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationStateRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.CoordinationStateProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.InstanceIdGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider.DatabaseCoordinationProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider.InMemoryCoordinationProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Auto-configuration for task coordination runtime beans.
 * <p>
 * Provides database-backed coordination by default, can be overridden via property:
 * lastfm.coordination.provider=in-memory
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(CoordinationProperties.class)
@Slf4j
public class TaskCoordinationAutoConfiguration {

    /**
     * Database coordination provider - used by default in production.
     */
    @Bean
    @ConditionalOnProperty(name = "lastfm.coordination.provider", havingValue = "database", matchIfMissing = true)
    @ConditionalOnMissingBean(CoordinationStateProvider.class)
    public CoordinationStateProvider databaseCoordinationProvider(
            CoordinationStateRepository stateRepository,
            CoordinationInstanceRepository instanceRepository,
            CoordinationRunningTaskRepository runningTaskRepository,
            String coordinationInstanceId,
            CoordinationProperties properties
    ) {
        log.info("Configuring DATABASE coordination provider");
        return new DatabaseCoordinationProvider(
                stateRepository,
                instanceRepository,
                runningTaskRepository,
                coordinationInstanceId,
                properties.getHeartbeat().getStaleTimeoutSeconds()
        );
    }

    /**
     * In-memory coordination provider - used for local development.
     */
    @Bean
    @ConditionalOnProperty(name = "lastfm.coordination.provider", havingValue = "in-memory")
    @ConditionalOnMissingBean(CoordinationStateProvider.class)
    public CoordinationStateProvider inMemoryCoordinationProvider() {
        log.info("Configuring IN-MEMORY coordination provider");
        return new InMemoryCoordinationProvider();
    }

    /**
     * Executor service for task execution.
     */
    @Bean(name = "tasksExecutor")
    @ConditionalOnMissingBean(name = "tasksExecutor")
    public ExecutorService tasksExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("tasks-" + t.threadId());
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Instance ID for coordination.
     */
    @Bean
    @ConditionalOnMissingBean(name = "coordinationInstanceId")
    public String coordinationInstanceId(@Value("${spring.application.name}") String applicationName) {
        String instanceId = InstanceIdGenerator.generate(applicationName);
        log.info("Generated coordination instance ID: {}", instanceId);
        return instanceId;
    }

    /**
     * Task coordinator - main service bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public TaskCoordinator taskCoordinator(
            CoordinationStateProvider stateProvider,
            String coordinationInstanceId,
            ExecutorService tasksExecutor
    ) {
        return new TaskCoordinator(stateProvider, coordinationInstanceId, tasksExecutor);
    }
}
