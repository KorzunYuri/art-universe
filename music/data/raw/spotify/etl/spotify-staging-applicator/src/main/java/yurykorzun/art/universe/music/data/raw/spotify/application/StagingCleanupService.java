package yurykorzun.art.universe.music.data.raw.spotify.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StagingCleanupService {

    private final SpotifyStagingIterationRepository iterationRepository;
    private final JdbcTemplate jdbc;
    private final long retentionHours;

    public StagingCleanupService(
        SpotifyStagingIterationRepository iterationRepository,
        JdbcTemplate jdbc,
        @Value("${spotify.staging.cleanup.completed-retention-hours}") long retentionHours
    ) {
        this.iterationRepository = iterationRepository;
        this.jdbc = jdbc;
        this.retentionHours = retentionHours;
    }

    @Scheduled(
        fixedDelayString = "${spotify.scheduling.staging-cleanup.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void cleanupOldStagingTables() {
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
