package yurykorzun.art.universe.common.pgnotify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.regex.Pattern;

@Component
@Slf4j
public class PgNotifyAfterCommitListener {

    private static final Pattern VALID_CHANNEL = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final JdbcTemplate jdbcTemplate;

    public PgNotifyAfterCommitListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotify(PgNotifyEvent event) {
        String channel = event.getChannel();
        if (!VALID_CHANNEL.matcher(channel).matches()) {
            log.error("Invalid NOTIFY channel name: '{}' — skipping", channel);
            return;
        }
        try {
            jdbcTemplate.execute("NOTIFY " + channel);
            log.debug("Sent NOTIFY on channel '{}'", channel);
        } catch (Exception e) {
            log.warn("Failed to send NOTIFY on channel '{}': {}", channel, e.getMessage());
        }
    }
}
