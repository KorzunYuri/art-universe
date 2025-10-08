package yurykorzun.art.universe.common.test.db;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that combines all necessary annotations for JPA tests with PostgreSQL TestContainers.
 * Also includes @PostgresTestContainer configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@PostgresTestContainer
public @interface PostgresJpaTest {
    
    // Делегируем параметры к @PostgresTestContainer
    String databaseName() default "test_db";
    String username() default "postgres";
    String password() default "postgres";
    String initScript() default "";
    String schema() default "";
}
