package yurykorzun.art.universe.common.test.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostgresTestContainer {
    String databaseName() default "test_db";
    String username() default "test_user";
    String password() default "test_password";
    String initScript() default "";
    String schema() default "";
}
