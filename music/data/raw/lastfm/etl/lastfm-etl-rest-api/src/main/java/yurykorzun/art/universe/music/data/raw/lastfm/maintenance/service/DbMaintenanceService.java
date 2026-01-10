package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DbMaintenanceService {

    private final TaskCoordinator dbMaintenanceCoordinator;
    private final DbMaintenanceExecutor dbMaintenanceExecutor;

    public DbMaintenanceService(
        TaskCoordinator taskCoordinator,
        DbMaintenanceExecutor dbMaintenanceExecutor
    ) {
        this.dbMaintenanceCoordinator = taskCoordinator;
        this.dbMaintenanceExecutor = dbMaintenanceExecutor;
    }

    @Scheduled(cron = "${lastfm.scheduling.maintenance.cron}")
    public boolean enqueueMaintenance() {
        log.info("requesting maintenance");
        try {
            dbMaintenanceCoordinator.requestMaintenance(dbMaintenanceExecutor::performMaintenance);
            return true;
        } catch (IllegalStateException e) {
            log.error("failed to execute maintenance due to error: {}", e.getMessage());
            return false;
        }
    }

}
