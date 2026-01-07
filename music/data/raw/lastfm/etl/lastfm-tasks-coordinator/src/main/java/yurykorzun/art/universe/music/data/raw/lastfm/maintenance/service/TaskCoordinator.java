package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.CoordinationStateProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicket;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Coordinates database-related tasks, including maintenance, to run in a queue.
 * <p>
 * Uses CoordinationStateProvider for state management, allowing both single-instance
 * (in-memory) and multi-instance (database-backed) coordination.
 * <p>
 * Configured as a bean in TaskCoordinationAutoConfiguration or TaskCoordinationTestConfig.
 */
@Slf4j
public class TaskCoordinator {

    private final CoordinationStateProvider stateProvider;
    private final String instanceId;
    private final DbTaskQueue maintenanceQueue = new DbTaskQueue();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ExecutorService taskExecutor;

    public TaskCoordinator(
            CoordinationStateProvider stateProvider,
            String instanceId,
            ExecutorService taskExecutor
    ) {
        this.stateProvider = stateProvider;
        this.instanceId = instanceId;
        this.taskExecutor = taskExecutor;
        log.info("TaskCoordinator initialized with instance ID: {}", instanceId);
    }

    /**
     * Sends heartbeat to coordination provider.
     * For database providers, this updates the instance heartbeat timestamp.
     * For in-memory providers, this is a no-op.
     * <p>
     * Also checks if maintenance can begin. This is necessary to handle the case where
     * maintenance was requested while other instances had running tasks. The requesting
     * instance needs to periodically retry until all tasks complete.
     */
    @Scheduled(fixedRateString = "${lastfm.coordination.heartbeat.interval-seconds:30}", timeUnit = TimeUnit.SECONDS)
    public void sendHeartbeat() {
        stateProvider.heartbeat(instanceId);
        triggerMaintenanceIfNeeded();
    }

    /**
     * Cleanup stale tasks from crashed instances.
     * For database providers, this removes tasks where instance heartbeat is stale.
     * For in-memory providers, this is a no-op.
     */
    @Scheduled(fixedRateString = "${lastfm.coordination.cleanup.interval-seconds:60}", timeUnit = TimeUnit.SECONDS)
    public void cleanupStaleTasks() {
        stateProvider.cleanupStaleTasks();
    }

    public void executeIfAllowed(Runnable task, String taskKey) {
        Optional<TaskExecutionTicket> ticketOpt = stateProvider.tryAcquireTaskExecution(taskKey, instanceId);

        if (ticketOpt.isEmpty()) {
            log.debug("Task execution denied for key: {}", taskKey);
            return;
        }

        TaskExecutionTicket ticket = ticketOpt.get();

        taskExecutor.submit(() -> {
            try {
                task.run();
            } finally {
                stateProvider.releaseTaskExecution(ticket);
                if (stateProvider.getRunningTaskCount() == 0) {
                    triggerMaintenanceIfNeeded();
                }
            }
        });
    }

    public void requestMaintenance(Runnable maintenanceTask) throws IllegalStateException {
        stateProvider.requestMaintenance(instanceId);
        maintenanceQueue.offer(maintenanceTask);

        if (stateProvider.getRunningTaskCount() == 0) {
            triggerMaintenanceIfNeeded();
        }
    }

    private void triggerMaintenanceIfNeeded() {
        if (stateProvider.getStatus() != CoordinationStatus.REQUESTED) {
            return;
        }

        // Only the instance that requested maintenance should execute it
        String requestingInstance = stateProvider.getRequestingInstanceId();
        if (requestingInstance == null || !requestingInstance.equals(instanceId)) {
            log.debug("Maintenance requested by another instance ({}), skipping", requestingInstance);
            return;
        }

        // Try to begin maintenance (only succeeds if no tasks running)
        if (!stateProvider.tryBeginMaintenance()) {
            log.debug("Cannot begin maintenance: tasks still running or another instance started maintenance");
            return;
        }

        log.info("Maintenance started by instance {}", instanceId);

        executor.submit(() -> {
            try {
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
            } finally {
                stateProvider.completeMaintenance();
                log.info("Maintenance completed by instance {}", instanceId);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
        taskExecutor.shutdownNow();
    }
}
