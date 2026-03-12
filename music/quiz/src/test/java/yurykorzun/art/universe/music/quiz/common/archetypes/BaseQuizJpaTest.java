package yurykorzun.art.universe.music.quiz.common.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.common.test.db.PostgresTestContainer;
import yurykorzun.art.universe.common.test.archetypes.BaseTest;

/**
 * Base class for persistence layer testing.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@PostgresTestContainer(
    databaseName = "art_universe",
    username = "mu_quiz_dm",
    password = "test_password",
    schema = "mu_quiz",
    initScript = "db/mu-view-stubs.sql"
)
@Tag("integration")
public abstract class BaseQuizJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(BaseQuizJpaTest.class, registry);
    }
}
