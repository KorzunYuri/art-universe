package yurykorzun.art.universe.common.test.db;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresDynamicPropertyConfigurer {

    public static void register(
        Class<?> testClass,
        DynamicPropertyRegistry registry
    ) {
        PostgresTestContainer cfg = AnnotatedElementUtils.findMergedAnnotation(testClass, PostgresTestContainer.class);

        if (cfg == null) {
            throw new IllegalStateException("Missing @PostgresTestContainer on " + testClass.getName());
        }

        PostgreSQLContainer<?> container = PostgresContainerManager.get(cfg);

        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", cfg::username);
        registry.add("spring.datasource.password", cfg::password);

        if (!cfg.schema().isEmpty()) {
            registry.add("spring.jpa.properties.hibernate.default_schema", cfg::schema);
        }
    }

    private PostgresDynamicPropertyConfigurer() {}
}
