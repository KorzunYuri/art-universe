package yurykorzun.art.universe.common.test.db;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PostgresTestContainerHolder {
    
    private static final String POSTGRES_IMAGE_NAME = "postgres:14-alpine";
    private static final ConcurrentHashMap<String, PostgreSQLContainer<?>> containers = new ConcurrentHashMap<>();
    
    public static PostgreSQLContainer<?> getContainer(Class<?> testClass) {
        // first look for @PostgresJpaTest
        PostgresJpaTest jpaTestConfig = testClass.getAnnotation(PostgresJpaTest.class);
        if (jpaTestConfig != null) {
            String key = generateKey(jpaTestConfig.databaseName(), jpaTestConfig.username(), jpaTestConfig.initScript());
            return containers.computeIfAbsent(key, k -> createContainer(
                jpaTestConfig.databaseName(), 
                jpaTestConfig.username(), 
                jpaTestConfig.password(), 
                jpaTestConfig.initScript()
            ));
        }
        
        // then look for @PostgresTestContainer
        PostgresTestContainer config = testClass.getAnnotation(PostgresTestContainer.class);
        if (config != null) {
            String key = generateKey(config.databaseName(), config.username(), config.initScript());
            return containers.computeIfAbsent(key, k -> createContainer(
                config.databaseName(), 
                config.username(), 
                config.password(), 
                config.initScript()
            ));
        }
        
        throw new IllegalStateException("Test class must be annotated with @PostgresJpaTest or @PostgresTestContainer");
    }
    
    private static PostgreSQLContainer<?> createContainer(String databaseName, String username, String password, String initScript) {
        log.info("Creating PostgreSQL container: db={}, user={}, initScript={}", 
                databaseName, username, initScript);
                
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withReuse(true);
                
        if (!initScript.isEmpty()) {
            container.withInitScript(initScript);
        }
        
        container.start();
        log.info("PostgreSQL container started: {}", container.getJdbcUrl());
        return container;
    }
    
    private static String generateKey(String databaseName, String username, String initScript) {
        return databaseName + ":" + username + ":" + initScript;
    }
}
