package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmPerformerProperty;

import java.time.Instant;

@Component
@Slf4j
public class LastfmApiCallExecutionScheduler {

    private final LastfmCallsOrchestrator orchestrator;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public LastfmApiCallExecutionScheduler(
        LastfmCallsOrchestrator orchestrator,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.orchestrator = orchestrator;
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
        long delaySecs = configPropertyHolder.getInt(LastfmPerformerProperty.SCHEDULE_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("start API calls performing");
            orchestrator.orchestrateApiCalls();
            log.info("finished API calls performing");
        } catch (Exception ex) {
            log.error("Error during API calls performing", ex);
        } finally {
            scheduleNext();
        }
    }
}
