package yurykorzun.art.universe.music.quiz.common.archetypes;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.common.test.db.PostgresTestContainer;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseTest;

// TODO rename class
/**
 * Base class for persistence layer testing.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@PostgresTestContainer(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    schema = "mu_quiz"
)
public abstract class BaseQuizJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(BaseQuizJpaTest.class, registry);
    }
}
