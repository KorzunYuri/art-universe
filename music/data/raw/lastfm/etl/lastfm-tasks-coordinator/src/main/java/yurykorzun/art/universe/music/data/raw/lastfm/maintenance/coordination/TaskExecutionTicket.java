package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination;

import java.time.Instant;

/**
 * Ticket representing permission to execute a task.
 * Must be released after task completion.
 */
public interface TaskExecutionTicket {

    /**
     * @return unique identifier for the task
     */
    String getTaskKey();

    /**
     * @return identifier of the instance executing this task
     */
    String getInstanceId();

    /**
     * @return timestamp when the task execution was acquired
     */
    Instant getAcquiredAt();
}
