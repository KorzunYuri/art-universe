package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DbMaintenanceService {

    private final TaskCoordinator dbMaintenanceCoordinator;
    private final JdbcTemplate jdbc;
    private final MusicDataIntegrationService musicDataIntegrationService;

    @Value("${lastfm.threshold.artist.listenersCount}")
    private int artistThreshold;
    @Value("${lastfm.threshold.album.playCount}")
    private int albumThreshold;
    @Value("${lastfm.threshold.track.playCount}")
    private int trackThreshold;
    @Value("${lastfm.threshold.tag.usageCount}")
    private int tagThreshold;

    public DbMaintenanceService(
            TaskCoordinator taskCoordinator,
            JdbcTemplate jdbc,
            MusicDataIntegrationService musicDataIntegrationService) {
        this.dbMaintenanceCoordinator = taskCoordinator;
        this.jdbc = jdbc;
        this.musicDataIntegrationService = musicDataIntegrationService;
    }

    @Scheduled(cron = "${lastfm.scheduling.maintenance.cron}")
    public boolean enqueueMaintenance() {
        log.info("requesting maintenance");
        try {
            dbMaintenanceCoordinator.requestMaintenance(this::executeMaintenanceTasks);
            return true;
        } catch (IllegalStateException e) {
            log.error("failed to execute maintenance due to error: {}", e.getMessage());
            return false;
        }
    }

    private void executeMaintenanceTasks() {
        // Execute cleanup for each entity type and collect cleanup run IDs
        Long artistCleanupRunId = cleanupEntity(LastfmEntityType.ARTIST, artistThreshold);
        Long albumCleanupRunId = cleanupEntity(LastfmEntityType.ALBUM, albumThreshold);
        Long trackCleanupRunId = cleanupEntity(LastfmEntityType.TRACK, trackThreshold);
        Long tagCleanupRunId = cleanupEntity(LastfmEntityType.TAG, tagThreshold);

        // Unbind deleted entities from music-data
        unbindDeletedEntitiesFromCleanupRun(artistCleanupRunId);
        unbindDeletedEntitiesFromCleanupRun(albumCleanupRunId);
        unbindDeletedEntitiesFromCleanupRun(trackCleanupRunId);
        unbindDeletedEntitiesFromCleanupRun(tagCleanupRunId);

        performDatabaseOptimization();
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
        Long cleanupRunId = (Long) results.get(0).get("cleanup_run_id");
        
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
