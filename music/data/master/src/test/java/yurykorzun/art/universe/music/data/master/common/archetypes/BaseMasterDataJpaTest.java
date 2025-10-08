package yurykorzun.art.universe.music.data.master.common.archetypes;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import yurykorzun.art.universe.common.test.db.PostgresJpaTest;
import yurykorzun.art.universe.common.test.db.PostgresTestContainerHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseTest;

@PostgresJpaTest(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    username = "mu_dm",
    password = "mu_dm_password",
    schema = "mu"
)
public abstract class BaseMasterDataJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> container = PostgresTestContainerHolder.getContainer(BaseMasterDataJpaTest.class);
        registry.add("spring.datasource.url", () -> container.getJdbcUrl() + "?currentSchema=mu");
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}