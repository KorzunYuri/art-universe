package yurykorzun.art.universe.music.data.raw.spotify.test.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.test.archetypes.BaseTest;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.music.data.raw.spotify.test.db.SpotifyPostgresTestContainer;

/**
 * Base class for Spotify JPA repository tests with PostgreSQL TestContainer.
 * Combines @DataJpaTest for JPA slice testing with Spotify-specific database configuration.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpotifyPostgresTestContainer
@Tag("integration")
public abstract class SpotifyJpaTest extends BaseTest {

    @MockitoBean
    protected ConfigPropertyHolder configPropertyHolder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(SpotifyJpaTest.class, registry);
    }
}
