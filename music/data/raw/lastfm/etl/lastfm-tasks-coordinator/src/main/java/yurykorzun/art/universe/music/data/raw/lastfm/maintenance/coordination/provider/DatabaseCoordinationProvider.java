package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationInstanceEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStateEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationInstanceRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationRunningTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationStateRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.CoordinationStateProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicket;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicketImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of CoordinationStateProvider for distributed coordination.
 * <p>
 * Enables multi-instance coordination with:
 * - Heartbeat-based crash detection
 * - Automatic cleanup of stale tasks
 * - Distributed maintenance mode enforcement
 * <p>
 * Configured as a bean in TaskCoordinationAutoConfiguration.
 */
@Slf4j
public class DatabaseCoordinationProvider implements CoordinationStateProvider {

    private final CoordinationStateRepository stateRepository;
    private final CoordinationInstanceRepository instanceRepository;
    private final CoordinationRunningTaskRepository runningTaskRepository;
    private final String instanceId;
    private final int staleTimeoutSeconds;

    public DatabaseCoordinationProvider(
            CoordinationStateRepository stateRepository,
            CoordinationInstanceRepository instanceRepository,
            CoordinationRunningTaskRepository runningTaskRepository,
            String instanceId,
            int staleTimeoutSeconds
    ) {
        this.stateRepository = stateRepository;
        this.instanceRepository = instanceRepository;
        this.runningTaskRepository = runningTaskRepository;
        this.instanceId = instanceId;
        this.staleTimeoutSeconds = staleTimeoutSeconds;
    }

    @PostConstruct
    @Transactional
    public void registerInstance() {
        Instant now = Instant.now();
        CoordinationInstanceEntity instance = CoordinationInstanceEntity.builder()
                .instanceId(instanceId)
                .moduleName(extractModuleName(instanceId))
                .hostName(extractHostName(instanceId))
                .startTime(now)
                .lastHeartbeat(now)
                .build();

        instanceRepository.save(instance);
        log.info("Registered instance in database: {}", instanceId);
    }

    @PreDestroy
    @Transactional
    public void unregisterInstance() {
        // Check if this instance requested/is running maintenance - reset state if so
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        if (state.getStatus() != CoordinationStatus.NORMAL &&
            instanceId.equals(state.getUpdatedByInstance())) {
            log.warn("Instance {} shutting down while in {} state - resetting to NORMAL",
                    instanceId, state.getStatus());
            state.setStatus(CoordinationStatus.NORMAL);
            state.setUpdatedAt(Instant.now());
            state.setUpdatedByInstance(null);
            stateRepository.save(state);
        }

        instanceRepository.deleteById(instanceId);
        log.info("Unregistered instance from database: {}", instanceId);
    }

    @Override
    @Transactional
    public Optional<TaskExecutionTicket> tryAcquireTaskExecution(String taskKey, String instanceId) {
        // Check coordination status first
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        if (state.getStatus() != CoordinationStatus.NORMAL) {
            log.debug("Task execution denied for key '{}': status is {}", taskKey, state.getStatus());
            return Optional.empty();
        }

        // Try to atomically acquire task
        Instant now = Instant.now();
        int inserted = runningTaskRepository.tryInsertTask(taskKey, instanceId, now);

        if (inserted == 0) {
            log.debug("Task '{}' is already running", taskKey);
            return Optional.empty();
        }

        log.debug("Task '{}' acquired for execution by instance {}", taskKey, instanceId);
        TaskExecutionTicket ticket = new TaskExecutionTicketImpl(taskKey, instanceId, now);
        return Optional.of(ticket);
    }

    @Override
    @Transactional
    public void releaseTaskExecution(TaskExecutionTicket ticket) {
        runningTaskRepository.deleteById(ticket.getTaskKey());
        log.debug("Task '{}' released", ticket.getTaskKey());
    }

    @Override
    @Transactional(readOnly = true)
    public int getRunningTaskCount() {
        return (int) runningTaskRepository.count();
    }

    @Override
    @Transactional
    public boolean requestMaintenance(String requestingInstanceId) {
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        if (state.getStatus() != CoordinationStatus.NORMAL) {
            throw new IllegalStateException("Maintenance already requested or running: " + state.getStatus());
        }

        state.setStatus(CoordinationStatus.REQUESTED);
        state.setUpdatedAt(Instant.now());
        state.setUpdatedByInstance(requestingInstanceId);
        stateRepository.save(state);

        log.info("Maintenance requested by instance {}", requestingInstanceId);
        return true;
    }

    @Override
    @Transactional
    public boolean tryBeginMaintenance() {
        // Check no tasks running
        long count = runningTaskRepository.count();
        if (count > 0) {
            log.debug("Cannot begin maintenance: {} tasks still running", count);
            return false;
        }

        // Try to transition REQUESTED → RUNNING
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        if (state.getStatus() != CoordinationStatus.REQUESTED) {
            log.debug("Cannot begin maintenance: status is {}, expected {}", state.getStatus(), CoordinationStatus.REQUESTED);
            return false;
        }

        state.setStatus(CoordinationStatus.RUNNING);
        state.setUpdatedAt(Instant.now());
        state.setUpdatedByInstance(instanceId);
        stateRepository.save(state);

        log.info("Maintenance started by instance {}", instanceId);
        return true;
    }

    @Override
    @Transactional
    public void completeMaintenance() {
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        state.setStatus(CoordinationStatus.NORMAL);
        state.setUpdatedAt(Instant.now());
        state.setUpdatedByInstance(instanceId);
        stateRepository.save(state);

        log.info("Maintenance completed by instance {}", instanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinationStatus getStatus() {
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        return state.getStatus();
    }

    @Override
    public String getRequestingInstanceId() {
        CoordinationStateEntity state = stateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

        return state.getUpdatedByInstance();
    }

    @Override
    @Transactional
    public void heartbeat(String instanceId) {
        int updated = instanceRepository.updateHeartbeat(instanceId, Instant.now());
        if (updated == 0) {
            log.warn("Heartbeat failed: instance {} not found in database", instanceId);
        } else {
            log.trace("Heartbeat sent for instance {}", instanceId);
        }
    }

    @Override
    @Transactional
    public void cleanupStaleTasks() {
        Instant threshold = Instant.now().minusSeconds(staleTimeoutSeconds);
        List<String> staleInstances = instanceRepository.findStaleInstances(threshold);

        if (!staleInstances.isEmpty()) {
            log.info("Found {} stale instances, removing them: {}", staleInstances.size(), staleInstances);

            // Check if any stale instance was the one that requested/is running maintenance
            CoordinationStateEntity state = stateRepository.findById(1)
                    .orElseThrow(() -> new IllegalStateException("Coordination state not initialized"));

            if (state.getStatus() != CoordinationStatus.NORMAL &&
                state.getUpdatedByInstance() != null &&
                staleInstances.contains(state.getUpdatedByInstance())
            ) {
                // Requesting/running instance crashed - reset to NORMAL
                log.warn("Stale instance {} was in {} state - resetting to {}",
                        state.getUpdatedByInstance(), state.getStatus(), CoordinationStatus.NORMAL);
                state.setStatus(CoordinationStatus.NORMAL);
                state.setUpdatedAt(Instant.now());
                state.setUpdatedByInstance(null);
                stateRepository.save(state);
            }

            instanceRepository.deleteAllById(staleInstances);
            // CASCADE delete will automatically remove their tasks
            log.info("Cleanup completed: removed {} stale instances", staleInstances.size());
        }
    }

    private String extractModuleName(String instanceId) {
        // Format: {module-name}--{hostname}--{pid}--{timestamp}
        String[] parts = instanceId.split("--");
        if (parts.length >= 1) {
            return parts[0];
        }
        return "unknown";
    }

    private String extractHostName(String instanceId) {
        // Format: {module-name}--{hostname}--{pid}--{timestamp}
        String[] parts = instanceId.split("--");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }
}
