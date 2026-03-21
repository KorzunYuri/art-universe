package yurykorzun.art.universe.music.data.raw.spotify.test.db;

import yurykorzun.art.universe.common.test.db.PostgresTestContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that configures PostgreSQL TestContainer for Spotify tests.
 * Can be used with both @DataJpaTest and @SpringBootTest.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PostgresTestContainer(
    databaseName = "art_universe",
    username = "mu_raw_spotify_dm",
    password = "test_password",
    schema = "mu_raw_spotify"
)
public @interface SpotifyPostgresTestContainer {
}
