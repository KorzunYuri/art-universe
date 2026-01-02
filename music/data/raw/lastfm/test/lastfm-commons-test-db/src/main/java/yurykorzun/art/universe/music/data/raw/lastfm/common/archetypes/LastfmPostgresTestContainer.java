package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import yurykorzun.art.universe.common.test.db.PostgresTestContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that configures PostgreSQL TestContainer for Lastfm tests.
 * Can be used with both @DataJpaTest and @SpringBootTest.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PostgresTestContainer(
    databaseName = "music_universe",
    initScript = "db/init-schema.sql",
    username = "mu_raw_lastfm_dm",
    password = "mu_raw_lastfm_dm_password",
    schema = "mu_raw_lastfm"
)
public @interface LastfmPostgresTestContainer {
}
