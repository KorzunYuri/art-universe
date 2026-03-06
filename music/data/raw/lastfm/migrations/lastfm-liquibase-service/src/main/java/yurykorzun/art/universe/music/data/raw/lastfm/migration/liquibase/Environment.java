package yurykorzun.art.universe.music.data.raw.lastfm.migration.liquibase;

public class Environment {

    public static final String ENV_VAR_DB_HOST = "AU_DB_MASTER_HOST";
    public static final String ENV_VAR_DB_PORT = "AU_DB_MASTER_PORT";
    public static final String ENV_VAR_DB_NAME = "AU_DB_NAME";
    public static final String ENV_VAR_DB_SCHEMA = "MURAW_LASTFM_DB_SCHEMA";
    public static final String ENV_VAR_DB_USER_NAME = "MURAW_LASTFM_DB_WRITER_USERNAME";
    public static final String ENV_VAR_DB_USER_PASSWORD = "MURAW_LASTFM_DB_WRITER_PASSWORD";
    public static final String ENV_VAR_MIGRATIONS_PATH = "MURAW_LASTFM_DB_MIGRATIONS_PATH";

    public static String getEnvValue(String varName) {
        var val = System.getenv(varName);
        if (val == null) {
            throw new IllegalArgumentException(String.format("env variable %s is not provided", varName));
        }
        return val;
    }
}
