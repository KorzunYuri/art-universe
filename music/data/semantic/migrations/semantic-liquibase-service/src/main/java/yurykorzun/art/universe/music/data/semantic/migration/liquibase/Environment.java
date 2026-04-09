package yurykorzun.art.universe.music.data.semantic.migration.liquibase;

public class Environment {

    public static final String ENV_VAR_DB_HOST = "AU_DB_MASTER_HOST";
    public static final String ENV_VAR_DB_PORT = "AU_DB_MASTER_PORT";
    public static final String ENV_VAR_DB_NAME = "AU_DB_NAME";
    public static final String ENV_VAR_DB_SCHEMA = "MU_SA_DB_SCHEMA";
    public static final String ENV_VAR_DB_USER_NAME = "MU_SA_DB_USERNAME";
    public static final String ENV_VAR_DB_USER_PASSWORD = "MU_SA_DB_PASSWORD_DM";
    public static final String ENV_VAR_MIGRATIONS_PATH = "MU_SA_DB_MIGRATIONS_PATH";

    public static String getEnvValue(String varName) {
        var val = System.getenv(varName);
        if (val == null) {
            throw new IllegalArgumentException(String.format("env variable %s is not provided", varName));
        }
        return val;
    }
}
