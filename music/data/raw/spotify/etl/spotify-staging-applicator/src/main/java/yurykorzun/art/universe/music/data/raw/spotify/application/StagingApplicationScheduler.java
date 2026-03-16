package yurykorzun.art.universe.music.data.raw.spotify.application;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyApplicatorProperty;

import java.time.Instant;

@Component
@Slf4j
public class StagingApplicationScheduler {

    private final StagingApplicationService applicationService;
    private final SearchReconciliationService searchReconciliationService;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public StagingApplicationScheduler(
        StagingApplicationService applicationService,
        SearchReconciliationService searchReconciliationService,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.applicationService = applicationService;
        this.searchReconciliationService = searchReconciliationService;
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
        long delaySecs = configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_APPLY_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("start staging application");
            applicationService.applySealedIterations();
            searchReconciliationService.reconcileMatchedAttempts();
            log.info("finished staging application");
        } catch (Exception ex) {
            log.error("Error during staging application", ex);
        } finally {
            scheduleNext();
        }
    }
}
