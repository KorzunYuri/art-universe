package yurykorzun.art.universe.music.data.raw.spotify.application;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyApplicatorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
public class StagingCleanupService {

    private final SpotifyStagingIterationRepository iterationRepository;
    private final JdbcTemplate jdbc;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public StagingCleanupService(
        SpotifyStagingIterationRepository iterationRepository,
        JdbcTemplate jdbc,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.iterationRepository = iterationRepository;
        this.jdbc = jdbc;
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
        long delaySecs = configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_DELAY_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            cleanupOldStagingTables();
        } catch (Exception ex) {
            log.error("Error during staging cleanup", ex);
        } finally {
            scheduleNext();
        }
    }

    public void cleanupOldStagingTables() {
        long retentionHours = configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_RETENTION_HOURS);
        Instant cutoff = Instant.now().minus(retentionHours, ChronoUnit.HOURS);
        List<StagingIteration> old = iterationRepository.findAllByStatusInAndSealedAtBefore(
            List.of(StagingIterationStatus.COMPLETED, StagingIterationStatus.FAILED), cutoff);

        if (old.isEmpty()) {
            return;
        }

        log.info("Dropping staging tables for {} old iterations", old.size());
        for (StagingIteration iteration : old) {
            dropStagingTables(iteration.getId());
        }
    }

    private void dropStagingTables(long iterationId) {
        for (String prefix : new String[]{"stg_artist", "stg_album", "stg_track", "stg_entity_relation"}) {
            String tableName = prefix + "_" + iterationId;
            try {
                jdbc.execute("DROP TABLE IF EXISTS " + tableName);
                log.debug("Dropped staging table {}", tableName);
            } catch (Exception e) {
                log.warn("Failed to drop staging table {}: {}", tableName, e.getMessage());
            }
        }
    }
}
