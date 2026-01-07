package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.CoordinationStateProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicket;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicketImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory implementation of CoordinationStateProvider.
 * <p>
 * This implementation is used for:
 * <ul>
 *   <li>Local development</li>
 *   <li>Testing</li>
 *   <li>Single-instance deployments</li>
 * </ul>
 * <p>
 * State is held in-memory and is not shared across instances.
 * Heartbeat and cleanup operations are no-ops.
 * <p>
 * Configured as a bean in TaskCoordinationAutoConfiguration or TaskCoordinationTestConfig.
 */
@Slf4j
public class InMemoryCoordinationProvider implements CoordinationStateProvider {

    private final AtomicInteger runningTasks = new AtomicInteger(0);
    private final ConcurrentHashMap<String, TaskExecutionTicketImpl> runningTaskMap = new ConcurrentHashMap<>();
    private final AtomicReference<CoordinationStatus> status = new AtomicReference<>(CoordinationStatus.NORMAL);
    private final AtomicReference<String> requestingInstanceId = new AtomicReference<>(null);

    @Override
    public Optional<TaskExecutionTicket> tryAcquireTaskExecution(String taskKey, String instanceId) {
        // Check status is NORMAL
        if (status.get() != CoordinationStatus.NORMAL) {
            log.debug("Task execution denied for key '{}': maintenance in progress/requested", taskKey);
            return Optional.empty();
        }

        // Try to insert into map (atomic check-and-set)
        TaskExecutionTicketImpl ticket = new TaskExecutionTicketImpl(taskKey, instanceId, Instant.now());
        TaskExecutionTicketImpl existing = runningTaskMap.putIfAbsent(taskKey, ticket);

        if (existing != null) {
            // Task already running
            log.debug("Task '{}' is already running", taskKey);
            return Optional.empty();
        }

        // Success - increment counter
        runningTasks.incrementAndGet();
        log.debug("Task '{}' acquired for execution (running tasks: {})", taskKey, runningTasks.get());
        return Optional.of(ticket);
    }

    @Override
    public void releaseTaskExecution(TaskExecutionTicket ticket) {
        runningTaskMap.remove(ticket.getTaskKey());
        int count = runningTasks.decrementAndGet();
        log.debug("Task '{}' released (running tasks: {})", ticket.getTaskKey(), count);
    }

    @Override
    public int getRunningTaskCount() {
        return runningTasks.get();
    }

    @Override
    public boolean requestMaintenance(String requestingInstance) {
        CoordinationStatus current = status.get();
        if (current != CoordinationStatus.NORMAL) {
            throw new IllegalStateException("Maintenance already requested or running");
        }

        boolean success = status.compareAndSet(CoordinationStatus.NORMAL, CoordinationStatus.REQUESTED);
        if (success) {
            requestingInstanceId.set(requestingInstance);
            log.info("Maintenance requested by instance {}", requestingInstance);
        }
        return success;
    }

    @Override
    public boolean tryBeginMaintenance() {
        // Check no tasks running
        if (runningTasks.get() > 0) {
            log.debug("Cannot begin maintenance: {} tasks still running", runningTasks.get());
            return false;
        }

        // Transition REQUESTED → RUNNING
        boolean success = status.compareAndSet(CoordinationStatus.REQUESTED, CoordinationStatus.RUNNING);
        if (success) {
            log.info("Maintenance started");
        }
        return success;
    }

    @Override
    public void completeMaintenance() {
        status.set(CoordinationStatus.NORMAL);
        requestingInstanceId.set(null);
        log.info("Maintenance completed");
    }

    @Override
    public CoordinationStatus getStatus() {
        return status.get();
    }

    @Override
    public String getRequestingInstanceId() {
        return requestingInstanceId.get();
    }

    @Override
    public void heartbeat(String instanceId) {
        // No-op for in-memory provider (single instance)
    }

    @Override
    public void cleanupStaleTasks() {
        // No-op for in-memory provider (single instance, no crash scenarios)
    }
}
