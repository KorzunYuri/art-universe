# Commons Test DB

Provides common database test utilities and containerized PostgreSQL via TestContainers for integration tests requiring a real database.

## Features

### Containerized PostgreSQL for Tests

**When to use**: For integration tests involving persistence layer on a real database.

#### Key Components

- [PostgresContainerManager.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresContainerManager.java) - Manages PostgreSQL container lifecycle
  - Creates and caches containers based on configuration
  - Reuses containers across tests with same configuration

- [PostgresTestContainer.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresTestContainer.java) - Annotation for database configuration
  - `databaseName` - Database name
  - `username` / `password` - Database credentials
  - `schema` - Optional default schema
  - `initScript` - Optional SQL script to run on container startup

- [PostgresDynamicPropertyConfigurer.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresDynamicPropertyConfigurer.java) - Spring property configuration helper
  - Reads `@PostgresTestContainer` from test class (supports meta-annotations)
  - Gets/creates container via `PostgresContainerManager`
  - Configures Spring datasource properties automatically

#### How It Works

1. Test class is annotated with `@PostgresTestContainer` (directly or via meta-annotation)
2. `PostgresDynamicPropertyConfigurer.register(testClass, registry)` is called in `@DynamicPropertySource` method
3. Configurer uses `AnnotatedElementUtils.findMergedAnnotation()` to find the annotation (supports meta-annotations)
4. `PostgresContainerManager` creates or reuses container based on configuration
5. Spring datasource properties are automatically configured from the container

#### Domain-Specific Usage Pattern

Create domain-specific meta-annotation and base test classes:

```java
// 1. Create meta-annotation for your domain configuration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@PostgresTestContainer(
    databaseName = "your_database",
    initScript = "db/init-schema.sql",
    username = "your_user",
    password = "your_password",
    schema = "your_schema"
)
public @interface YourDomainPostgresTestContainer {
}

// 2. Create base class for JPA slice tests
@DataJpaTest
@YourDomainPostgresTestContainer
public abstract class YourDomainJpaTest extends BaseTest {
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(YourDomainJpaTest.class, registry);
    }
}

// 3. Create base class for full context tests
@SpringBootTest
@YourDomainPostgresTestContainer
public abstract class YourDomainContextTestWithDb extends BaseTest {
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresDynamicPropertyConfigurer.register(YourDomainContextTestWithDb.class, registry);
        registry.add("server.port", () -> 0); // Optional: random port for tests
    }
}
```

#### Examples

- **LastFM Module**:
  - Meta-annotation: [LastfmPostgresTestContainer.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmPostgresTestContainer.java)
  - JPA tests: [LastfmJpaTest.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmJpaTest.java)
  - Full context: [LastfmContextTestWithDb.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmContextTestWithDb.java)

### Transaction Logging for Tests

- [TransactionListener.java](src/main/java/yurykorzun/art/universe/common/test/db/TransactionListener.java) - Logs transaction events (before commit, after commit/rollback)

## Related Documentation

- [Database Testing Patterns](../../../docs/kb/patterns/backend/testing/testing-with-persistence-layer.md) - Complete pattern documentation
