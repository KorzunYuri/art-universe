package yurykorzun.art.universe.music.quiz.common.archetypes;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import yurykorzun.art.universe.common.test.db.PostgresJpaTest;
import yurykorzun.art.universe.common.test.db.PostgresTestContainerHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseTest;

/**
 * Base class for persistence layer testing.
 */
@PostgresJpaTest(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    schema = "mu_quiz"
)
public abstract class JpaOnlyTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> container = PostgresTestContainerHolder.getContainer(JpaOnlyTest.class);
        registry.add("spring.datasource.url", () -> container.getJdbcUrl() + "?currentSchema=mu_quiz");
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}
