# JPA Repository Testing Pattern

## Overview

Pattern for using Spring JPA layer in integration tests using TestContainers PostgreSQL with annotation-based configuration.

**Key features**:
- Real PostgreSQL via TestContainers (not H2/embedded DB)
- Container reuse across test suite for performance
- Annotation-based configuration
- Domain-specific base classes with schema isolation
- Optional init scripts and Liquibase migrations


## How It Works

1. Test class annotated with `@PostgresJpaTest` provides configuration (database name, schema, credentials, init script)
2. `PostgresTestContainerHolder` generates cache key from configuration
3. If container with same key exists, it's reused; otherwise, new container is created
4. TestContainer starts PostgreSQL
5. Init-script is executed, if provided. It may, for example, create database and set necessary permissions
6. Liquibase runs migrations, if configured
7. Database is ready with complete schema
8. Tests execute against real database
9. Container cleaned up after tests


## Generic Foundation

The pattern is built on common infrastructure modules that provide:

[PostgresJpaTest.java](../../../../../common/test/commons-test-db/src/main/java/yurykorzun/art/universe/common/test/db/PostgresJpaTest.java) - Meta-annotation combining:
- [PostgresTestContainer.java](../../../../../common/test/commons-test-db/src/main/java/yurykorzun/art/universe/common/test/db/PostgresTestContainer.java) - Annotation for Postgres container configuration parameters
- `@DataJpaTest` - Loads only JPA components (entities, repositories)
- `@Testcontainers` - Enables TestContainers support
- `@AutoConfigureTestDatabase(replace = NONE)` - Uses TestContainers instead of embedded DB

[PostgresTestContainerHolder.java](../../../../common/test/commons-test-db/src/main/java/yurykorzun/art/universe/common/test/db/PostgresTestContainerHolder.java:8) - Singleton container manager:
- Creates a new PostgreSQL 14 Alpine container per consumer
- Reuses container across tests with same configuration
- Manages container lifecycle

[BaseTest.java](../../../../../common/test/commons-test/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/BaseTest.java):
- Activates 'test' Spring profile 
- Loads common components into the test context


## How to Use

Create a base class extending BaseTest 

**Pattern**:
```java
@PostgresJpaTest(
    databaseName = "your_database",
    initScript = "db/init-schema.sql",     // optional
    username = "your_user",
    password = "your_password",
    schema = "your_schema"                 // optional
)
public abstract class YourDomainJpaTest extends BaseTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> container = PostgresTestContainerHolder.getContainer(YourDomainJpaTest.class);
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}
```


## Domain Implementations

- **LastFM**: [LastfmJpaTest](../../../../music/data/raw/lastfm/test/lastfm-commons-test-db/README.md) - Init script + Liquibase migrations
- **Quiz**: [JpaOnlyTest](../../../../music/quiz/src/test/java/yurykorzun/art/universe/music/quiz/common/archetypes/JpaOnlyTest.java) - Init script only
- **Master Data**: [BaseMasterDataJpaTest](../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/common/archetypes/BaseMasterDataJpaTest.java) - Init script only

## Related Patterns

- [Testing Overview](overview.md) - Test naming conventions and categories
- [Controller Testing](testing-controllers.md) - MVC layer testing
