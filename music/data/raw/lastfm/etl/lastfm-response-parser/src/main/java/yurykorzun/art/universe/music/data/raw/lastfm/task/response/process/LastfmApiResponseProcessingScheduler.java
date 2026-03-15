package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmParserProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

import java.time.Instant;

@Component
@Slf4j
public class LastfmApiResponseProcessingScheduler {

    public static final String TASK_NAME_API_RESPONSES_PROCESSING = "api-responses-processing";

    private final LastfmApiResponseService apiResponseService;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public LastfmApiResponseProcessingScheduler(
        LastfmApiResponseService apiResponseService,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.apiResponseService = apiResponseService;
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
        long delaySecs = configPropertyHolder.getInt(LastfmParserProperty.SCHEDULE_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("start API responses processing");
            apiResponseService.processResponses();
            log.info("finished API responses processing");
        } catch (Exception ex) {
            log.error("Error during API responses processing", ex);
        } finally {
            scheduleNext();
        }
    }
}
