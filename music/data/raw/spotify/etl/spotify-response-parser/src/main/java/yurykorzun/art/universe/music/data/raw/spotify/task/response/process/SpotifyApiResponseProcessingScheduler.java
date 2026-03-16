package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyParserProperty;

import java.time.Instant;

@Component
@Slf4j
public class SpotifyApiResponseProcessingScheduler {

    private final SpotifyApiResponseProcessingOrchestrator orchestrator;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public SpotifyApiResponseProcessingScheduler(
        SpotifyApiResponseProcessingOrchestrator orchestrator,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.orchestrator = orchestrator;
        this.taskScheduler = taskScheduler;
        this.configPropertyHolder = configPropertyHolder;
    }

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
        long delaySecs = configPropertyHolder.getInt(SpotifyParserProperty.SCHEDULE_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("start API responses processing");
            orchestrator.processResponses();
            log.info("finished API responses processing");
        } catch (Exception ex) {
            log.error("Error during API responses processing", ex);
        } finally {
            scheduleNext();
        }
    }
}
