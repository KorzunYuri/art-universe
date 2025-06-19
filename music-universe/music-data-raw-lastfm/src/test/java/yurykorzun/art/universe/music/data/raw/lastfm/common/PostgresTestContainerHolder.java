package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.Getter;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainerHolder {

    private PostgresTestContainerHolder() {}

    private static final String POSTGRES_IMAGE_NAME = "postgres:14-alpine";

    @Getter
    static final PostgreSQLContainer<?> container;

    static {
        container = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
                .withDatabaseName("music_universe")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        container.start();
    }

}
