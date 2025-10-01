package yurykorzun.art.universe.music.data.raw.lastfm.migration.liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;

import static yurykorzun.art.universe.music.data.raw.lastfm.migration.liquibase.Environment.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.migration.liquibase.Environment.getEnvValue;

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
            Liquibase liquibase = new Liquibase(migrationsPath, new ClassLoaderResourceAccessor(), database);
            
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
