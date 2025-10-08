package yurykorzun.art.universe.music.quiz.common.archetypes;


import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import yurykorzun.art.universe.common.config.CommonConfig;

/**
 * Base class for persistence layer testing.
 */
@ActiveProfiles("test")
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    CommonConfig.class,
})
public abstract class JpaOnlyTest {

    private static String IMAGE_NAME = "postgres:14-alpine";
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withDatabaseName("music_universe")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("db/init-schema.sql")
                .withReuse(true);
        POSTGRESQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        final String jdbcUrl = POSTGRESQL_CONTAINER.getJdbcUrl();

        registry.add("spring.datasource.url", () -> jdbcUrl + "?currentSchema=mu_quiz");
        registry.add("spring.datasource.username", () -> "mu_quiz_dm");
        registry.add("spring.datasource.password", () -> "mu_quiz_dm");
    }

}
