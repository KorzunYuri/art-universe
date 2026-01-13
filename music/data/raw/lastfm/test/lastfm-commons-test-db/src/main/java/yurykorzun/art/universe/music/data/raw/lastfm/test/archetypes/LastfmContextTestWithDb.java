package yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.archetypes.BaseTest;
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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(LastfmContextTestWithDb.class, registry);

        registry.add("server.port", () -> 0);
    }
}
