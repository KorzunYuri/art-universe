package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class MaintenanceOrchestrator {

    private final DbMaintenanceExecutor dbMaintenanceExecutor;
    private final ExecutorService maintenanceExecutor;
    private final AtomicBoolean maintenanceInProgress;

    public MaintenanceOrchestrator(
        DbMaintenanceExecutor dbMaintenanceExecutor
    ) {
        this.dbMaintenanceExecutor = dbMaintenanceExecutor;
        this.maintenanceExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("lastfm-db-maintenance-executor");
            t.setDaemon(false);
            return t;
        });
        this.maintenanceInProgress = new AtomicBoolean(false);
    }

    /**
     * Scheduled maintenance trigger via cron.
     * Delegates to requestMaintenance() for actual execution.
     */
    @Scheduled(cron = "${lastfm.scheduling.maintenance.cron}")
    public void scheduledMaintenance() {
        log.info("scheduled maintenance triggered");
        requestMaintenance();
    }

    /**
     * Requests maintenance execution. Returns immediately without blocking the caller.
     *
     * @return true if maintenance was submitted for execution, false if already running
     */
    public boolean requestMaintenance() {
        if (maintenanceInProgress.compareAndSet(false, true)) {
            log.info("requesting maintenance - submitting to executor");
            maintenanceExecutor.submit(() -> {
                try {
                    log.info("starting maintenance execution");
                    dbMaintenanceExecutor.performMaintenance();
                    log.info("maintenance execution completed successfully");
                } catch (Exception e) {
                    log.error("maintenance execution failed: {}", e.getMessage(), e);
                } finally {
                    maintenanceInProgress.set(false);
                    log.info("maintenance flag cleared");
                }
            });
            return true;
        } else {
            log.warn("maintenance already in progress - request ignored");
            return false;
        }
    }

    /**
     * For testing purposes - check if maintenance is currently running.
     *
     * @return true if maintenance is in progress
     */
    public boolean isMaintenanceInProgress() {
        return maintenanceInProgress.get();
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutting down maintenance executor");
        maintenanceExecutor.shutdown();
        try {
            if (!maintenanceExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("maintenance executor did not terminate in time - forcing shutdown");
                maintenanceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("interrupted while waiting for maintenance executor shutdown", e);
            maintenanceExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
