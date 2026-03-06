# LastFM Commons Test DB

Database integration test support for LastFM modules using the [Database Testing Patterns](../../../../../../docs/kb/patterns/backend/testing/testing-with-persistence-layer.md) with LastFM-specific schema and Liquibase migrations.

If you need to create and save entities while keeping DB in consistent state in your tests, use [lastfm-commons-test-db-helper](../lastfm-commons-test-db-helper/README.md) extension.


## LastFM-Specific Configuration

- Database: `art_universe`
- Schema: `mu_raw_lastfm` (created via init script)
- Credentials: `mu_raw_lastfm_dm` / `mu_raw_lastfm_dm_password`
- Init Script: [db/init-schema.sql](src/main/resources/db/init-schema.sql) - Creates schemas and sets permissions
- Liquibase: Configured in [application-test.yml](src/main/resources/application-test.yml) - Applies full LastFM schema from [lastfm-liquibase-resources](../../migrations/lastfm-liquibase-resources/README.md)


## Base Test Classes

### LastfmJpaTest - JPA Slice Tests (@DataJpaTest)

Use for repository and JPA-related tests that only need persistence layer.
Non-JPA bean must to be imported via `@Import` annotation.

[LastfmJpaTest.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/archetypes/LastfmJpaTest.java)

```java
@Import(
    LastfmApiCallServiceImpl.class
)
class LastfmApiCallRepositoryTest extends LastfmJpaTest {

    @Autowired
    private LastfmApiCallRepository repository; // loaded by DataJpaTest slice
    
    @Autowired
    private LastfmApiCallServiceImpl service; // requres manual importing

    @Test
    void saveAll_shouldSaveApiCalls_whenValidDataProvided() {
        // Test implementation
    }
}
```

If you need to inject a component living outside the persistence layer, use `@Import` annotation on your test class.


### LastfmContextTestWithDb - Full Context Tests (@SpringBootTest)

Use for tests requiring full Spring application context with database.

[LastfmContextTestWithDb.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/archetypes/LastfmContextTestWithDb.java)

```java
class LastfmApiCallGeneratorsRegistryTest extends LastfmContextTestWithDb {

    @Test
    void shouldReturnCorrectGeneratorForEachCallType() {
        // Validates that registry returns Spring proxies with AOP
        BaseLastfmApiCallGenerator generator =
            LastfmApiCallGeneratorsRegistry.get(LastfmApiCallType.TAG_TOP_TAGS);

        assertTrue(AopUtils.isAopProxy(generator));
    }
}
```


## Configuration Details

The module provides [LastfmPostgresTestContainer.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/db/LastfmPostgresTestContainer.java) meta-annotation that encapsulates TestContainer configuration.

This meta-annotation is used by both `LastfmJpaTest` and `LastfmContextTestWithDb` base classes. 

## Related Documentation

- [Database Testing Patterns](../../../../../../docs/kb/patterns/backend/testing/testing-with-persistence-layer.md) - Generic pattern documentation
- [LastFM Modules Overview](../../README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)
