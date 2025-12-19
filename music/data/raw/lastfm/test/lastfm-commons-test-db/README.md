# LastFM Commons Test DB

Database integration test support for LastFM modules using the [JPA Repository Testing Pattern](../../../../../../docs/kb/patterns/backend/testing/jpa-repository-tests.md) with LastFM-specific schema and Liquibase migrations.

If you need to create and save entities while keeping DB in consistent state in your tests, use [lastfm-commons-test-db-helper](../lastfm-commons-test-db-helper/README.md) extension.


## LastFM-Specific Configuration

- Database: `music_universe`
- Schema: `mu_raw_lastfm` (created via init script)
- Credentials: `mu_raw_lastfm_dm` / `mu_raw_lastfm_dm_password`
- Init Script: [db/init-schema.sql](src/main/resources/db/init-schema.sql) - Creates schemas and sets permissions
- Liquibase: Configured in [application-test.yml](src/main/resources/application-test.yml) - Applies full LastFM schema from [lastfm-liquibase-resources](../../migrations/lastfm-liquibase-resources/README.md)


### How to Use

Extend [LastfmJpaTest](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmJpaTest.java) for repository tests:

```java
@Tag("integration")
class LastfmApiCallRepositoryTest extends LastfmJpaTest {

    @Autowired
    private LastfmApiCallRepository repository;

    @Test
    void saveAll_shouldSaveApiCalls_whenValidDataProvided() {
        // Test implementation
    }
}
```

If you need to inject a component living out of the persistence layer, use @Import annotation on your test class.


## Related Documentation

- [JPA Repository Testing Pattern](../../../../../../docs/kb/patterns/backend/testing/testing-with-persistence-layer.md) - Generic pattern documentation
- [LastFM Modules Overview](../../README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)
