package yurykorzun.art.universe.music.data.master.common.archetypes;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import yurykorzun.art.universe.common.test.db.PostgresDynamicPropertyConfigurer;
import yurykorzun.art.universe.common.test.db.PostgresTestContainer;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseTest;

// TODO rename class
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@PostgresTestContainer(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    username = "mu_dm",
    password = "mu_dm_password",
    schema = "mu"
)
public abstract class BaseMasterDataJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(BaseMasterDataJpaTest.class, registry);
    }
}