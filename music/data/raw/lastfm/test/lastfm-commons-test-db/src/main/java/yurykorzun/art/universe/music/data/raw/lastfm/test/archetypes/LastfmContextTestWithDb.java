package yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.test.archetypes.BaseTest;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.music.data.raw.lastfm.test.db.LastfmPostgresTestContainer;

/**
 * Base class for Spring Boot integration tests with PostgreSQL TestContainer.
 * Combines @SpringBootTest for full application context with Lastfm-specific database configuration.
 *
 * <p>Use this base class when you need to test components that require the full Spring context
 * (e.g., testing registries, AOP aspects, schedulers) with a real database.</p>
 */
@SpringBootTest
@LastfmPostgresTestContainer
@Tag("integration")
public abstract class LastfmContextTestWithDb extends BaseTest {

    @MockitoBean
    protected ConfigPropertyHolder configPropertyHolder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(LastfmContextTestWithDb.class, registry);

        registry.add("server.port", () -> 0);
    }
}
