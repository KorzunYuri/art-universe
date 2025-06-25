package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordinates database-related tasks, including maintenance, to run in a queue
 */
@Component
@Slf4j
public class DbMaintenanceCoordinator {
    
    private enum Status { NORMAL, REQUESTED, RUNNING }

    private final AtomicInteger runningTasks = new AtomicInteger(0);
    private final DbTaskQueue maintenanceQueue = new DbTaskQueue();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Status status = Status.NORMAL;
    private Set<String> runningTaskNames = new HashSet<>();

    public void executeIfAllowed(Runnable task) {
        executeUniqueIfAllowed(task, UUID.randomUUID().toString());
    }

    public void executeUniqueIfAllowed(Runnable task, String taskKey) {
        if (status != Status.NORMAL) {
            log.warn("Maintenance in progress/requested — skipping task");
            return;
        }

        if (runningTaskNames.contains(taskKey)) {
            log.warn("Task {} is already running", taskKey);
            return;
        }

        runningTaskNames.add(taskKey);
        runningTasks.incrementAndGet();
        try {
            task.run();
        } finally {
            runningTaskNames.remove(taskKey);
            if (runningTasks.decrementAndGet() == 0) {
                triggerMaintenanceIfNeeded();
            }
        }
    }

    public void requestMaintenance(Runnable maintenanceTask) throws IllegalStateException {
        synchronized (this) {
            if (status != Status.NORMAL) {
                throw new IllegalStateException("Maintenance already requested or running");
            }
            status = Status.REQUESTED;
            maintenanceQueue.offer(maintenanceTask);
            if (runningTasks.get() == 0) {
                triggerMaintenanceIfNeeded();
            }
        }
    }

    private void triggerMaintenanceIfNeeded() {
        synchronized (this) {
            if (status != Status.REQUESTED || runningTasks.get() != 0) {
                return;
            }
            status = Status.RUNNING;
        }

        executor.submit(() -> {
            Runnable task;
            while ((task = maintenanceQueue.poll()) != null) {
                try {
                    log.info("Starting maintenance task");
                    task.run();
                    log.info("Finished maintenance task");
                } catch (Exception e) {
                    log.error("Error during maintenance task", e);
                }
            }
            status = Status.NORMAL;
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
