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
  - `databaseName` - Database name (container key — shared across modules using the same DB)
  - `username` / `password` - Module-specific DB credentials (NOT the container superuser)
  - `schema` - Optional default schema (`hibernate.default_schema`)
  - `initScript` - Optional classpath path to a SQL file executed once per container after the
    centralized `01-init.sh` completes. Use for **cross-module stubs** and other things that live neither in centralized script not in migrations (e.g. `mu_view` table
    shapes needed by quiz). All DDL in these scripts must use `IF NOT EXISTS`.

- [PostgresDynamicPropertyConfigurer.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresDynamicPropertyConfigurer.java) - Spring property configuration helper
  - Reads `@PostgresTestContainer` from test class (supports meta-annotations)
  - Gets/creates container via `PostgresContainerManager`
  - Configures Spring datasource properties automatically

#### Two-Tier DB Initialization

| Tier | What | Where |
|------|------|-------|
| **Tier 1 — Centralized** | Databases, users, schemas, infra grants | `env/docker/common/db/initdb/01-init.sh` + `changesets/` |
| **Tier 2 — Module stubs** | Cross-module objects not in Tier 1 or Liquibase | `src/test/resources/db/<name>.sql` via `initScript` |

Tier 1 is the single source of truth for infrastructure — no module should duplicate user/schema creation.
Tier 2 is only needed when a module's Liquibase migrations reference objects owned by a *different* module
(e.g. quiz migrations reference `mu_view` views created by master-data Liquibase, which does not run in quiz tests).

**Rules for `initScript` SQL:**
- All DDL must use `IF NOT EXISTS` — the container is reused between runs
- Do not create users or schemas — those belong in Tier 1 (`01-init.sh` changesets)
- Grant any cross-schema `SELECT` access that the module's Liquibase or runtime queries need

#### How It Works

1. Test class is annotated with `@PostgresTestContainer` (directly or via meta-annotation)
2. `PostgresDynamicPropertyConfigurer.register(testClass, registry)` is called from `@DynamicPropertySource`
3. Configurer resolves the annotation via `AnnotatedElementUtils.findMergedAnnotation()` (supports meta-annotations)
4. `PostgresContainerManager.get(cfg)` returns a cached container keyed by `databaseName`, starting a new one if needed:
   - Copies only `01-init.sh` + `changesets/` from `env/docker/common/db/initdb/` into the container
   - `01-init.sh` creates all users, schemas and grants; passwords default to `test_password`
5. If `initScript` is set and not yet applied to this container, it is executed via `execInContainer` (psql)
6. Spring datasource properties are configured using the module-specific `username`/`password` from the annotation

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
    username = "your_user",
    password = "your_password",  // must match the default in 01-init.sh ("test_password")
    schema = "your_schema"
    // initScript = "db/stubs.sql"  -- only if Liquibase references cross-module objects
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

#### Real Examples

| Module | Base class | `initScript` |
|--------|-----------|-------------|
| `music:data:master` | `BaseMasterDataJpaTest` | — |
| `music:quiz` | `BaseQuizJpaTest` | `db/mu-view-stubs.sql` (stubs `mu_view` objects) |
| `music:data:raw:lastfm:*` | `LastfmJpaTest` / `LastfmContextTestWithDb` | — |

- **LastFM Module**:
  - Meta-annotation: [LastfmPostgresTestContainer.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmPostgresTestContainer.java)
  - JPA tests: [LastfmJpaTest.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmJpaTest.java)
  - Full context: [LastfmContextTestWithDb.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmContextTestWithDb.java)

### Transaction Logging for Tests

- [TransactionListener.java](src/main/java/yurykorzun/art/universe/common/test/db/TransactionListener.java) - Logs transaction events (before commit, after commit/rollback)

## Related Documentation

- [Database Testing Patterns](../../../docs/kb/patterns/backend/testing/testing-with-persistence-layer.md) - Complete pattern documentation
