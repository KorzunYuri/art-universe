package yurykorzun.art.universe.music.data.master.common;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestContainerHolder {

    private PostgresTestContainerHolder() {}

    private static String IMAGE_NAME = "postgres:14-alpine";
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_NAME))
            .withDatabaseName("mu")
            .withUsername("mu_dm")
            .withPassword("mu_dm_password")
            .withInitScript("db/init-schema.sql")
            .withReuse(true);
        POSTGRESQL_CONTAINER.start();
    }
}
