package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.time.Instant;

@Component
@Slf4j
public class SpotifyApiCallGenerationScheduler {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public SpotifyApiCallGenerationScheduler(
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.taskScheduler = taskScheduler;
        this.configPropertyHolder = configPropertyHolder;
    }

    @PostConstruct
    public void start() {
        scheduleNext();
    }

    @PreDestroy
    public void stop() {
        running = false;
    }

    private void scheduleNext() {
        if (!running) {
            return;
        }
        long delaySecs = configPropertyHolder.getInt(SpotifyGeneratorProperty.SCHEDULE_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("start API calls generation");
            SpotifyApiCallGeneratorsRegistry.getRegistry()
                .forEach((apiCallType, generator) -> {
                    if (!isGenerationEnabled(apiCallType)) {
                        log.debug("Generation disabled for call type {}", apiCallType);
                        return;
                    }
                    log.info("start API calls generation for method {}", generator.getApiCallType().getMethod());
                    generator.createApiCalls();
                    log.info("finished API calls generation for method {}", generator.getApiCallType().getMethod());
                });
            log.info("finished API calls generation");
        } catch (Exception ex) {
            log.error("Error during API calls generation", ex);
        } finally {
            scheduleNext();
        }
    }

    private boolean isGenerationEnabled(SpotifyApiCallType callType) {
        SpotifyGeneratorProperty prop = SpotifyGeneratorProperty.valueOf("GENERATE_" + callType.name());
        return configPropertyHolder.getBoolean(prop);
    }
}
