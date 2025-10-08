package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import yurykorzun.art.universe.common.test.db.PostgresJpaTest;
import yurykorzun.art.universe.common.test.db.PostgresTestContainerHolder;

@PostgresJpaTest(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    username = "mu_raw_lastfm_dm",
    password = "mu_raw_lastfm_dm_password",
    schema = "mu_quiz"
)
public abstract class LastfmJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> container = PostgresTestContainerHolder.getContainer(LastfmJpaTest.class);
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}
