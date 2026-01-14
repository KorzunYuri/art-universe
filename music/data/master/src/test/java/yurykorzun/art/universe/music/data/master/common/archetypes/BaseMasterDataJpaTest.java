package yurykorzun.art.universe.music.data.master.common.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.common.test.db.PostgresTestContainer;
import yurykorzun.art.universe.common.archetypes.BaseTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@PostgresTestContainer(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    username = "mu_dm",
    password = "mu_dm_password",
    schema = "mu"
)
@Tag("integration")
public abstract class BaseMasterDataJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(BaseMasterDataJpaTest.class, registry);
    }
}