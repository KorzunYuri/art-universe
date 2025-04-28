package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

@Service
@Slf4j
public class DbMaintenanceService {

    private final DbMaintenanceCoordinator dbMaintenanceCoordinator;
    private final JdbcTemplate jdbc;

    @Value("${lastfm.threshold.artist.listenersCount}")
    private int artistThreshold;
    @Value("${lastfm.threshold.album.playCount}")
    private int albumThreshold;
    @Value("${lastfm.threshold.track.playCount}")
    private int trackThreshold;
    @Value("${lastfm.threshold.tag.usageCount}")
    private int tagThreshold;

    public DbMaintenanceService(DbMaintenanceCoordinator dbMaintenanceCoordinator, JdbcTemplate jdbc) {
        this.dbMaintenanceCoordinator = dbMaintenanceCoordinator;
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "${scheduling.lastfm.tasks.maintenance.cron}", scheduler = "maintenanceScheduler")
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
        cleanupEntity(LastfmEntityType.ARTIST,  artistThreshold);
        cleanupEntity(LastfmEntityType.ALBUM,   albumThreshold);
        cleanupEntity(LastfmEntityType.TRACK,   trackThreshold);
        cleanupEntity(LastfmEntityType.TAG,     tagThreshold);

        jdbc.execute("VACUUM FULL");
        jdbc.execute("ANALYZE");
        // cannot reindex - ownership required
        // jdbc.execute("REINDEX DATABASE " +
        //     jdbc.queryForObject("SELECT current_database()", String.class));
    }

    private void cleanupEntity(LastfmEntityType entityType, int popularityAttrThreshold) {
        jdbc.execute(String.format("SELECT cleanup_entity(%d, %d, false)", entityType.getCode(), popularityAttrThreshold));
    }
}
