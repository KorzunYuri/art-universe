package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmParserProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor.LastfmAttributeHistoryProcessor;

import java.time.Instant;

@Component
@Slf4j
public class LastfmAttributeHistoryScheduler {

    private final LastfmAttributeHistoryProcessor processor;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public LastfmAttributeHistoryScheduler(
        LastfmAttributeHistoryProcessor processor,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.processor = processor;
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
        long delaySecs = configPropertyHolder.getInt(LastfmParserProperty.ATTRIBUTE_HISTORY_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            processor.triggerAttributeHistoryProcessing();
        } catch (Exception ex) {
            log.error("Error during attribute history processing", ex);
        } finally {
            scheduleNext();
        }
    }
}
