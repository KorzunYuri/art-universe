package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Profile("test")
@Slf4j
public class TransactionListener {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommit(ApplicationEvent event) {
        log.info("🚀 Перед коммитом транзакции");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(ApplicationEvent event) {
        log.info("✅ Транзакция закоммичена");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollback(ApplicationEvent event) {
        log.warn("❌ Транзакция откатилась!");
    }
}