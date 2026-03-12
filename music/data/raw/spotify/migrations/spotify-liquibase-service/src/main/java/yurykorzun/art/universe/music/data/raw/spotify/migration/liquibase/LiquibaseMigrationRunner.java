package yurykorzun.art.universe.music.data.raw.spotify.migration.liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import static yurykorzun.art.universe.music.data.raw.spotify.migration.liquibase.Environment.*;

public class LiquibaseMigrationRunner {

    public static void main(String[] args) {
        try {
            String host = getEnvValue(ENV_VAR_DB_HOST);
            String port = getEnvValue(ENV_VAR_DB_PORT);
            String dbName = getEnvValue(ENV_VAR_DB_NAME);
            String dbSchema = getEnvValue(ENV_VAR_DB_SCHEMA);
            String url = String.format("jdbc:postgresql://%s:%s/%s?currentSchema=%s", host, port, dbName, dbSchema);

            String username = getEnvValue(ENV_VAR_DB_USER_NAME);
            String password = System.getenv(ENV_VAR_DB_USER_PASSWORD);

            System.out.println("Connecting to: " + url);
            System.out.println("Username: " + username);

            Connection connection = DriverManager.getConnection(url, username, password);
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            String migrationsPath = getEnvValue(ENV_VAR_MIGRATIONS_PATH);

            ResourceAccessor resourceAccessor;
            File dockerResources = new File("/app/resources");
            if (dockerResources.exists()) {
                System.out.println("Using FileSystemResourceAccessor for Docker environment");
                resourceAccessor = new FileSystemResourceAccessor(dockerResources);
            } else {
                System.out.println("Using ClassLoaderResourceAccessor for local environment");
                resourceAccessor = new ClassLoaderResourceAccessor();
            }

            Liquibase liquibase = new Liquibase(migrationsPath, resourceAccessor, database);

            System.out.println("Running Liquibase migrations...");
            liquibase.update("");
            System.out.println("Migrations completed successfully!");

            connection.close();

        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
