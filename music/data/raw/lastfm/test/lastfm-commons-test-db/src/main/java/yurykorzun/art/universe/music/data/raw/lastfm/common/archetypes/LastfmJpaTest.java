package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;

/**
 * Base class for JPA repository tests with PostgreSQL TestContainer.
 * Combines @DataJpaTest for JPA slice testing with Lastfm-specific database configuration.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@LastfmPostgresTestContainer
public abstract class LastfmJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(LastfmJpaTest.class, registry);
    }
}
