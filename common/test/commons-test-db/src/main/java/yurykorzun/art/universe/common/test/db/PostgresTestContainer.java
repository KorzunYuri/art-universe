package yurykorzun.art.universe.common.test.db;

import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Testcontainers
public @interface PostgresTestContainer {

    String databaseName() default "test_db";
    String username() default "test_user";
    String password() default "test_password";
    String schema() default "";

    /**
     * Optional classpath path to a SQL script executed against the {@code databaseName} database
     * after the centralized 01-init.sh completes. Use for module-specific stubs (e.g. views from
     * sibling modules that this module's Liquibase migrations depend on).
     */
    String initScript() default "";
}
