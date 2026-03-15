package yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.test.archetypes.BaseTest;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.music.data.raw.lastfm.test.db.LastfmPostgresTestContainer;

/**
 * Base class for JPA repository tests with PostgreSQL TestContainer.
 * Combines @DataJpaTest for JPA slice testing with Lastfm-specific database configuration.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@LastfmPostgresTestContainer
@Tag("integration")
public abstract class LastfmJpaTest extends BaseTest {

    @MockitoBean
    protected ConfigPropertyHolder configPropertyHolder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(LastfmJpaTest.class, registry);
    }
}
