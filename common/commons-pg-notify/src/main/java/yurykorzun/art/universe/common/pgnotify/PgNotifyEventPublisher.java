package yurykorzun.art.universe.common.pgnotify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.regex.Pattern;

@Component
@Slf4j
public class PgNotifyEventPublisher {

    private static final Pattern VALID_CHANNEL = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    public PgNotifyEventPublisher(ApplicationEventPublisher eventPublisher, JdbcTemplate jdbcTemplate) {
        this.eventPublisher = eventPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sends a PostgreSQL {@code NOTIFY} on the given channel, adapting to the current context:
     * <ul>
     *   <li>Inside a transaction — defers NOTIFY to AFTER_COMMIT via {@link PgNotifyAfterCommitListener}</li>
     *   <li>Outside a transaction — executes NOTIFY immediately (data is already committed)</li>
     * </ul>
     */
    public void notifyAfterCommit(String channel) {
        if (!VALID_CHANNEL.matcher(channel).matches()) {
            log.error("Invalid NOTIFY channel name: '{}' — skipping", channel);
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            eventPublisher.publishEvent(new PgNotifyEvent(this, channel));
        } else {
            try {
                jdbcTemplate.execute("NOTIFY " + channel);
                log.debug("Sent NOTIFY on channel '{}'", channel);
            } catch (Exception e) {
                log.warn("Failed to send NOTIFY on channel '{}': {}", channel, e.getMessage());
            }
        }
    }
}
