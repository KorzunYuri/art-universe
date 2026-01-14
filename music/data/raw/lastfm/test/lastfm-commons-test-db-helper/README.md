# LastFM Commons Test DB Helper

The module provides helper utilities and methods for creating entities taking into account foreign keys for database integration tests.

It is built on top of [lastfm-commons-test-db](../lastfm-commons-test-db/README.md), a module that provides a containerized Postgres instance for tests.
To understand how database is set up, read the [base module README](../lastfm-commons-test-db/README.md) first.

## Key components

### Test Repositories

The module introduces default versions of repositories defined in [lastfm-repositories](../../lastfm-repositories/README.md) module with almost none additional methods.

The repositories don't replace the ones used in consuming modules, they are used solely for persisting entities that are necessary to create a specific state in database.

**location pattern**: `src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/**/repository/**/*`

### Consistency Helper

**Purpose**: helps to create test entities while avoiding foreign keys hell.

**Class**: [DbConsistencyHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/persistence/DbConsistencyHelper.java)

DbConsistencyHelper provides methods for creating and persisting an entity in one call. 
It utilizes [EntityCreationHelper.java](../lastfm-commons-test-jpa/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/domain/entity/EntityCreationHelper.java) for entity creation and adds save step.

There are two types of methods:
- createAndSave{Entity} - create and save an entity
- create{EntityForPersistence} - create an entity, persist all the entities it depends on but leave entity saving to the caller

The list of methods doesn't cover all the entities - it's rather based on usage and may be extended in the future.

Creation methods usually have two types of signatures:
- specific signatures for frequent cases (e.g. creating an entity with an API call of a specific type as its source)
- signatures with a builder consumer as the only arg, allowing the caller to apply customization on top of default values before saving

### Base Test Class

**Purpose**: providing a base test class with persistence layer and [Consistency Helper](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/persistence/DbConsistencyHelper.java) OOTB.

**Class**: [LastfmJpaTestHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/archetypes/LastfmJpaTestHelper.java)

Features:
- extends [LastfmJpaTest.java](../lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/archetypes/LastfmJpaTest.java) from [lastfm-commons-test-db](../lastfm-commons-test-db/README.md) module,
so Spring context limited to persistence layer with containerized database is loaded when the test starts
- adds [DbConsistencyHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/persistence/DbConsistencyHelper.java) and test repositories to the context
- autowires [DbConsistencyHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/persistence/DbConsistencyHelper.java) for usage in extending classes

#### How to use

- extend test class from [LastfmJpaTestHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/archetypes/LastfmJpaTestHelper.java)
- inject [DbConsistencyHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/persistence/DbConsistencyHelper.java)
- add DbConsistencyHelper#cleanup call to @BeforeEach or @AfterEach
- use [DbConsistencyHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/persistence/DbConsistencyHelper.java) to create entities for tests when needed

#### Example

```java
class LastfmAlbumRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @BeforeEach
    void setUp() {
        consistencyHelper.cleanup();
    }

    @Test
    void findAllToGetInfoFor_shouldDeduplicateByMbid_whenDuplicatesExist() {
        // use helper to create an entity for this test
        LastfmArtist artist1 = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Artist Name 1")
            .mbid("same-mbid-123")
            .approvalStatus(ApprovalStatus.PENDING)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
    );
```


## Related Documentation

- [LastFM Modules Overview](../../README.md)
- [LastFM Commons Test DB](../lastfm-commons-test-db/README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)
