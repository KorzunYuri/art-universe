package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;

/**
 * Base class for Spring Boot integration tests with PostgreSQL TestContainer.
 * Combines @SpringBootTest for full application context with Lastfm-specific database configuration.
 *
 * <p>Use this base class when you need to test components that require the full Spring context
 * (e.g., testing registries, AOP aspects, schedulers) with a real database.</p>
 */
@SpringBootTest
@LastfmPostgresTestContainer
public abstract class LastfmContextTestWithDb extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(LastfmContextTestWithDb.class, registry);

        registry.add("server.port", () -> 0);
    }
}
