package yurykorzun.art.universe.common.pgnotify;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.context.SmartLifecycle;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

/**
 * Manages a dedicated PostgreSQL connection for {@code LISTEN} and runs a work callback
 * when a notification arrives or when a fallback timeout expires.
 * <p>
 * The LISTEN connection is non-pooled (created via {@link DriverManager}) to avoid
 * interfering with HikariCP. On connection failure, the loop reconnects automatically
 * and continues executing work as a degraded polling fallback.
 */
@Slf4j
public class PgNotificationLoop implements SmartLifecycle {

    private static final long RECONNECT_DELAY_MS = 5000;

    private final String channel;
    private final Runnable workCallback;
    private final Supplier<Integer> fallbackTimeoutMsSupplier;
    private final DataSource dataSource;

    private volatile boolean running;
    private Thread listenerThread;
    private Connection listenConnection;

    public PgNotificationLoop(
        String channel,
        Runnable workCallback,
        Supplier<Integer> fallbackTimeoutMsSupplier,
        DataSource dataSource
    ) {
        this.channel = channel;
        this.workCallback = workCallback;
        this.fallbackTimeoutMsSupplier = fallbackTimeoutMsSupplier;
        this.dataSource = dataSource;
    }

    @Override
    public void start() {
        running = true;
        listenerThread = new Thread(this::listenLoop, "pg-listen-" + channel);
        listenerThread.setDaemon(true);
        listenerThread.start();
        log.info("Started PgNotificationLoop for channel '{}'", channel);
    }

    private void listenLoop() {
        while (running) {
            try {
                ensureConnection();
                PGConnection pgConn = listenConnection.unwrap(PGConnection.class);
                int timeoutMs = fallbackTimeoutMsSupplier.get();
                PGNotification[] notifications = pgConn.getNotifications(timeoutMs);
                if (notifications != null && notifications.length > 0) {
                    log.debug("Received {} notification(s) on channel '{}'",
                        notifications.length, channel);
                }
                safeExecuteWork();
            } catch (SQLException e) {
                log.warn("LISTEN connection lost for channel '{}': {}", channel, e.getMessage());
                closeConnection();
                sleepSafely(RECONNECT_DELAY_MS);
                safeExecuteWork();
            }
        }
        closeConnection();
        log.info("PgNotificationLoop stopped for channel '{}'", channel);
    }

    private void ensureConnection() throws SQLException {
        if (listenConnection == null || listenConnection.isClosed()) {
            if (dataSource instanceof HikariDataSource hds) {
                listenConnection = DriverManager.getConnection(
                    hds.getJdbcUrl(), hds.getUsername(), hds.getPassword());
            } else {
                throw new IllegalStateException(
                    "Cannot extract JDBC credentials — expected HikariDataSource but got "
                        + dataSource.getClass().getName());
            }
            listenConnection.setAutoCommit(true);
            try (Statement stmt = listenConnection.createStatement()) {
                stmt.execute("LISTEN " + channel);
            }
            log.info("Established LISTEN connection for channel '{}'", channel);
        }
    }

    private void safeExecuteWork() {
        try {
            workCallback.run();
        } catch (Exception e) {
            log.error("Work callback failed for channel '{}': {}", channel, e.getMessage(), e);
        }
    }

    private void closeConnection() {
        if (listenConnection != null) {
            try {
                listenConnection.close();
            } catch (SQLException e) {
                log.debug("Error closing LISTEN connection for '{}': {}", channel, e.getMessage());
            }
            listenConnection = null;
        }
    }

    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        log.info("Stopping PgNotificationLoop for channel '{}'", channel);
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }
}
