package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination;

import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStatus;

import java.util.Optional;

/**
 * Provides coordination state for TaskCoordinator.
 * <p>
 * Implementations must handle:
 * <ul>
 *   <li>Task execution gating (allow/deny based on maintenance state)</li>
 *   <li>Task deduplication (one task with given key running at a time)</li>
 *   <li>Maintenance state transitions (NORMAL → REQUESTED → RUNNING → NORMAL)</li>
 *   <li>Instance identification and heartbeat management</li>
 * </ul>
 * <p>
 * Thread Safety: All methods must be thread-safe.
 * <p>
 * Implementations:
 * <ul>
 *   <li>InMemoryCoordinationProvider: Single-instance in-memory state (dev/testing)</li>
 *   <li>DatabaseCoordinationProvider: Multi-instance database-backed state (production)</li>
 * </ul>
 */
public interface CoordinationStateProvider {

    /**
     * Attempts to register a task for execution.
     *
     * @param taskKey    unique identifier for the task (e.g., "api-calls-execution")
     * @param instanceId identifier of the instance attempting to run the task
     * @return TaskExecutionTicket if execution is allowed, empty if denied
     * <p>
     * Execution is denied if:
     * <ul>
     *   <li>Maintenance is in progress or requested</li>
     *   <li>Same taskKey is already running (anywhere in the cluster)</li>
     * </ul>
     */
    Optional<TaskExecutionTicket> tryAcquireTaskExecution(String taskKey, String instanceId);

    /**
     * Releases a task after execution completes.
     *
     * @param ticket the ticket obtained from tryAcquireTaskExecution
     */
    void releaseTaskExecution(TaskExecutionTicket ticket);

    /**
     * Gets current count of running tasks across all instances.
     *
     * @return number of tasks currently in execution
     */
    int getRunningTaskCount();

    /**
     * Requests maintenance mode.
     *
     * @param requestingInstanceId identifier of the instance requesting maintenance
     * @return true if transition to REQUESTED succeeded, false if already in REQUESTED or RUNNING
     * @throws IllegalStateException if maintenance already requested or running
     */
    boolean requestMaintenance(String requestingInstanceId);

    /**
     * Attempts to transition from REQUESTED to RUNNING state.
     * Only succeeds if no tasks are running.
     *
     * @return true if transition succeeded, false otherwise
     */
    boolean tryBeginMaintenance();

    /**
     * Completes maintenance and returns to NORMAL state.
     */
    void completeMaintenance();

    /**
     * Gets current coordination status.
     *
     * @return current status (NORMAL, REQUESTED, RUNNING)
     */
    CoordinationStatus getStatus();

    /**
     * Gets the instance ID that requested maintenance (if any).
     *
     * @return instance ID if maintenance is requested/running, null otherwise
     */
    String getRequestingInstanceId();

    /**
     * Heartbeat to indicate instance is alive.
     * For database providers, updates instance heartbeat timestamp.
     * For in-memory providers, this is a no-op.
     *
     * @param instanceId identifier of the instance
     */
    void heartbeat(String instanceId);

    /**
     * Cleanup stale tasks from crashed instances.
     * For database providers, removes tasks where instance heartbeat is stale.
     * For in-memory providers, this is a no-op.
     */
    void cleanupStaleTasks();
}
