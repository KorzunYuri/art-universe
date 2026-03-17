package yurykorzun.art.universe.common.pgnotify;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PgNotifyEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public PgNotifyEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a {@link PgNotifyEvent} that triggers a PostgreSQL {@code NOTIFY}
     * after the current transaction commits.
     * <p>
     * Must be called within a {@code @Transactional} method. The actual NOTIFY SQL
     * is executed by {@link PgNotifyAfterCommitListener} in the AFTER_COMMIT phase.
     */
    public void notifyAfterCommit(String channel) {
        eventPublisher.publishEvent(new PgNotifyEvent(this, channel));
    }
}
