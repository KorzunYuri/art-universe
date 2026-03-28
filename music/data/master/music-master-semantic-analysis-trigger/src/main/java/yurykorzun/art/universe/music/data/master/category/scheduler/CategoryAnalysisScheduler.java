package yurykorzun.art.universe.music.data.master.category.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.master.category.config.MasterTriggerProperty;
import yurykorzun.art.universe.music.data.master.category.scanner.MasterEntityScanner;

import java.time.Instant;

@Component
public class CategoryAnalysisScheduler {

    private static final Logger log = LoggerFactory.getLogger(CategoryAnalysisScheduler.class);

    private final MasterEntityScanner scanner;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public CategoryAnalysisScheduler(
        MasterEntityScanner scanner,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.scanner = scanner;
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
        long delaySecs = configPropertyHolder.getInt(MasterTriggerProperty.SCAN_INTERVAL_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("Starting master entity category analysis scan");
            int artists = scanner.scanArtists();
            log.info("Category analysis scan completed: {} artist tickets submitted", artists);
        } catch (Exception ex) {
            log.error("Error during master entity category analysis scan", ex);
        } finally {
            scheduleNext();
        }
    }
}
