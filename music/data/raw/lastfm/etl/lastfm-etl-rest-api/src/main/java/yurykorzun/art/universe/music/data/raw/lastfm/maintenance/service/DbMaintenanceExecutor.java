package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.observability.util.ObservabilityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DbMaintenanceExecutor {

    private final JdbcTemplate jdbc;
    private final MusicDataIntegrationService musicDataIntegrationService;
    private final ObservabilityService observabilityService;

    @Value("${lastfm.threshold.artist.listenersCount}")
    private int artistThreshold;
    @Value("${lastfm.threshold.album.playCount}")
    private int albumThreshold;
    @Value("${lastfm.threshold.track.playCount}")
    private int trackThreshold;
    @Value("${lastfm.threshold.tag.usageCount}")
    private int tagThreshold;

    public DbMaintenanceExecutor(
        JdbcTemplate jdbc,
        MusicDataIntegrationService musicDataIntegrationService,
        ObservabilityService observabilityService
    ) {
        this.jdbc = jdbc;
        this.musicDataIntegrationService = musicDataIntegrationService;
        this.observabilityService = observabilityService;
    }

    @Observed(name = "music.data.raw.lastfm.maintenance.process",
              contextualName = "lastfm-maintenance-process")
    public void performMaintenance() {

        Map.of(
            LastfmEntityType.ARTIST, artistThreshold,
            LastfmEntityType.ALBUM, albumThreshold,
            LastfmEntityType.TRACK, trackThreshold,
            LastfmEntityType.TAG, tagThreshold
        ).forEach((entityType, threshold) -> {
            Long cleanupRunId = observeCleanupEntity(entityType, threshold);
            observeUnbindEntity(entityType, cleanupRunId);
        });

        observeDatabaseOptimization();
    }

    private Long observeCleanupEntity(LastfmEntityType entityType, int popularityAttrThreshold) {
        return observabilityService.observe(
            "music.data.raw.lastfm.maintenance.entity.cleanup",
            () -> cleanupEntity(entityType, popularityAttrThreshold),
            o -> o
                .contextualName("lastfm-maintenance-entity-cleanup")
                .lowCardinalityKeyValue("entity_type", entityType.getName())
        );
    }

    private void observeUnbindEntity(LastfmEntityType entityType, Long cleanupRunId) {
        observabilityService.observe(
            "music.data.raw.lastfm.maintenance.entity.unbind",
            () -> unbindDeletedEntitiesFromCleanupRun(cleanupRunId),
            o -> o
                .contextualName("lastfm-maintenance-entity-unbind")
                .lowCardinalityKeyValue("entity_type", entityType.getName())
        );
    }

    private void observeDatabaseOptimization() {
        observabilityService.observe(
            "music.data.raw.lastfm.maintenance.db.optimization",
            this::performDatabaseOptimization,
            o -> o
                .contextualName("lastfm-maintenance-db-optimization")
        );
    }

    private void performDatabaseOptimization() {
        log.info("Executing VACUUM FULL");
        long vacuumStart = System.currentTimeMillis();
        jdbc.execute("VACUUM FULL");
        long vacuumDuration = System.currentTimeMillis() - vacuumStart;
        log.info("VACUUM FULL completed in {} ms", vacuumDuration);

        log.info("Executing ANALYZE");
        long analyzeStart = System.currentTimeMillis();
        jdbc.execute("ANALYZE");
        long analyzeDuration = System.currentTimeMillis() - analyzeStart;
        log.info("ANALYZE completed in {} ms", analyzeDuration);

        // cannot reindex - ownership required
        // jdbc.execute("REINDEX DATABASE " +
        //     jdbc.queryForObject("SELECT current_database()", String.class));
    }

    private Long cleanupEntity(LastfmEntityType entityType, int popularityAttrThreshold) {
        log.info("Starting cleanup for entity type {} with threshold {}", entityType, popularityAttrThreshold);

        List<Map<String, Object>> results = jdbc.queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            entityType.getCode(), popularityAttrThreshold, false
        );

        if (results.isEmpty()) {
            log.warn("No cleanup history returned for entity type {}", entityType);
            return null;
        }

        // Extract cleanup run ID from the first result
        Long cleanupRunId = (Long) results.getFirst().get("cleanup_run_id");

        log.info("Completed cleanup for entity type {} with run ID {}", entityType, cleanupRunId);

        // Log cleanup summary
        results.forEach(row -> {
            String message = (String) row.get("message");
            log.debug("Cleanup {}: {}", entityType, message);
        });

        return cleanupRunId;
    }

    private void unbindDeletedEntitiesFromCleanupRun(Long cleanupRunId) {
        if (cleanupRunId == null) {
            return;
        }

        log.info("Starting unbind process for cleanup run {}", cleanupRunId);

        // Get all entity types that were deleted in this cleanup run
        List<Integer> entityTypes = jdbc.queryForList(
            "SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?",
            Integer.class, cleanupRunId
        );

        for (Integer entityTypeCode : entityTypes) {
            LastfmEntityType entityType = CodedRegistry.getByCode(entityTypeCode, LastfmEntityType.class)
                .orElseThrow(() -> new IllegalArgumentException("Unknown entity type code: " + entityTypeCode));

            // Get entity IDs to unbind
            List<Long> entityIds = jdbc.queryForList(
                "SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?",
                Long.class, cleanupRunId, entityType.getCode()
            );

            if (!entityIds.isEmpty()) {
                musicDataIntegrationService.unbindEntities(entityType, entityIds);
            }
        }

        log.info("Completed unbind process for cleanup run {}", cleanupRunId);
    }

}
