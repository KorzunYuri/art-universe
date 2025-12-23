package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination;

import java.time.Instant;

/**
 * Implementation of TaskExecutionTicket.
 */
public record TaskExecutionTicketImpl(
        String taskKey,
        String instanceId,
        Instant acquiredAt
) implements TaskExecutionTicket {

    @Override
    public String getTaskKey() {
        return taskKey;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public Instant getAcquiredAt() {
        return acquiredAt;
    }
}
