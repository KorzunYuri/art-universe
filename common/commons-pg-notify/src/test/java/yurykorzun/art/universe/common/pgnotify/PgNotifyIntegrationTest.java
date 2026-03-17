package yurykorzun.art.universe.common.pgnotify;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.common.test.db.PostgresTestContainer;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PgNotifyIntegrationTest.TestConfig.class)
@PostgresTestContainer(databaseName = "pg_notify_test", username = "test", password = "test")
@Tag("integration")
class PgNotifyIntegrationTest {

    private static final String TEST_CHANNEL = "test_channel";
    private static final int FALLBACK_TIMEOUT_MS = 500;

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(PgNotifyIntegrationTest.class, registry);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PgNotifyEventPublisher publisher;

    @Test
    void notificationLoop_shouldExecuteWork_whenNotifyIsSent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        PgNotificationLoop loop = new PgNotificationLoop(
            TEST_CHANNEL,
            latch::countDown,
            () -> 30_000, // large fallback — we expect NOTIFY to wake it up
            dataSource
        );
        loop.start();
        try {
            // give listener thread time to establish connection and LISTEN
            Thread.sleep(200);

            jdbcTemplate.execute("NOTIFY " + TEST_CHANNEL);

            assertTrue(latch.await(5, TimeUnit.SECONDS),
                "Work callback should have been triggered by NOTIFY");
        } finally {
            loop.stop();
        }
    }

    @Test
    void notificationLoop_shouldExecuteWork_onFallbackTimeout() throws Exception {
        AtomicInteger callCount = new AtomicInteger();

        PgNotificationLoop loop = new PgNotificationLoop(
            TEST_CHANNEL,
            callCount::incrementAndGet,
            () -> FALLBACK_TIMEOUT_MS,
            dataSource
        );
        loop.start();
        try {
            // wait for at least 2 fallback cycles without sending any NOTIFY
            Thread.sleep(FALLBACK_TIMEOUT_MS * 4);

            int count = callCount.get();
            assertTrue(count >= 2,
                "Work callback should have been triggered at least twice by fallback timeout, but was called " + count + " times");
        } finally {
            loop.stop();
        }
    }

    @Test
    void publisher_shouldSendNotifyOutsideTransaction_andTriggerLoop() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        PgNotificationLoop loop = new PgNotificationLoop(
            TEST_CHANNEL,
            latch::countDown,
            () -> 30_000,
            dataSource
        );
        loop.start();
        try {
            Thread.sleep(200);

            // publisher called outside a transaction — should execute NOTIFY immediately
            publisher.notifyAfterCommit(TEST_CHANNEL);

            assertTrue(latch.await(5, TimeUnit.SECONDS),
                "PgNotifyEventPublisher should have triggered the notification loop");
        } finally {
            loop.stop();
        }
    }

    @Test
    void publisher_shouldRejectInvalidChannelName() {
        assertThrows(IllegalArgumentException.class,
            () -> publisher.notifyAfterCommit("'; DROP TABLE users; --"));
    }
}
