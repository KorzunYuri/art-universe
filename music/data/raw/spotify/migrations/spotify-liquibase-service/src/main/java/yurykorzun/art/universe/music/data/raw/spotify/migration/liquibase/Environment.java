package yurykorzun.art.universe.music.data.raw.spotify.migration.liquibase;

public class Environment {

    public static final String ENV_VAR_DB_HOST = "AU_DB_MASTER_HOST";
    public static final String ENV_VAR_DB_PORT = "AU_DB_MASTER_PORT";
    public static final String ENV_VAR_DB_NAME = "AU_DB_NAME";
    public static final String ENV_VAR_DB_SCHEMA = "MURAW_SPOTIFY_DB_SCHEMA";
    public static final String ENV_VAR_DB_USER_NAME = "MURAW_SPOTIFY_DB_WRITER_USERNAME";
    public static final String ENV_VAR_DB_USER_PASSWORD = "MURAW_SPOTIFY_DB_WRITER_PASSWORD";
    public static final String ENV_VAR_MIGRATIONS_PATH = "MURAW_SPOTIFY_DB_MIGRATIONS_PATH";

    public static String getEnvValue(String envVarName) {
        String value = System.getenv(envVarName);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Environment variable " + envVarName + " is not set");
        }
        return value;
    }
}
