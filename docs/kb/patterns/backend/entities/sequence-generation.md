# Sequence Generation Pattern

## Purpose

Establish a consistent pattern for generating primary key IDs using database sequences in JPA entities, ensuring unique ID allocation and preventing sequence conflicts.

## When to Use

- Creating any new JPA entity
- Defining primary key generation strategy
- Writing database migration for new entity tables
- Optimizing batch insert operations

---

## Pattern Overview

Every JPA entity must have:
1. **Dedicated database sequence** - One sequence per entity type
2. **JPA sequence configuration** - `@SequenceGenerator` annotation
3. **ID field with generation strategy** - `@GeneratedValue` annotation
4. **Proper allocation size** - Based on entity creation patterns

---

## Implementation Steps

### Step 1: Create Database Sequence

**Naming Convention**: `{entity_name}_seq`

**Examples**:
- `artist_seq` for Artist entity
- `album_seq` for Album entity
- `track_binding_seq` for TrackBinding entity

**See Migration Files**:
- [Album sequence creation](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changesets/0002-album/0002-0010-album$initial.sql)
- [Track sequence creation](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changesets/0004-track/0004-0010-track$initial.sql)

**Pattern**:
```sql
CREATE SEQUENCE {entity}_seq START 1 INCREMENT BY {allocationSize};

CREATE TABLE {entity} (
    id BIGINT NOT NULL,
    -- other columns
    PRIMARY KEY (id)
);
```

---

### Step 2: Configure JPA Entity

Add `@SequenceGenerator` and `@GeneratedValue` to the `id` field:

**Annotation Structure**:
```java
@Id
@SequenceGenerator(
    name = "{entity}_seq_gen",        // Generator name (can be anything)
    sequenceName = "{entity}_seq",    // Database sequence name
    allocationSize = 1 or 50          // How many IDs to pre-allocate
)
@GeneratedValue(
    strategy = GenerationType.SEQUENCE,
    generator = "{entity}_seq_gen"    // Must match @SequenceGenerator name
)
@Setter(value = AccessLevel.NONE)
private Long id;
```

**See Examples**:
- **allocationSize = 50**: [Album.java:15-23](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L15-L23)
- **allocationSize = 50**: [Track.java:15-23](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Track.java#L15-L23)
- **allocationSize = 50**: [LastfmArtist.java](../../../../../music/data/raw/lastfm/lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/entity/LastfmArtist.java)
- **allocationSize = 1**: [Game.java](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/Game.java)

---

## Allocation Size Configuration

The `allocationSize` parameter determines how many IDs are pre-allocated by Hibernate.

### For Single-Record Creation (allocationSize = 1)

**Use when**:
- Entities created one at a time via user actions
- Real-time entity creation through UI
- Low-volume entity creation

**Examples**:
- Game entities created on-demand
- Generation entities created per pipeline execution

**Benefit**: Database sequence stays in sync with actual IDs in the table

**See**: [Game.java](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/Game.java)

---

### For Batch Creation (allocationSize > 1)

**Use when**:
- Entities created in large batches
- Bulk data import from external APIs
- High-volume entity creation

**Examples**:
- Album/Track entities (allocationSize = 50)
- LastFM entities imported from API (allocationSize = 50)
- Bulk historical data import
- ETL processes

**Benefit**: Reduces database round-trips during batch inserts

**Trade-off**: Sequence values may have gaps if application restarts

**See Examples**:
- [Album.java:19](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L19)
- [Track.java:19](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Track.java#L19)
- [LastfmArtist.java](../../../../../music/data/raw/lastfm/lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/entity/LastfmArtist.java)

---

## Optional vs Required Relationships

### Self-Referencing with Optional Group ID

Both Album and Track entities use self-referencing relationships with optional group IDs:

**Album**: `albumGroupId` → links reissues/remasters to original album
**Track**: `trackGroupId` → links versions/remixes to original track

**See Implementation**:
- [Album.java:37-42](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L37-L42)
- [Track.java:37-42](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Track.java#L37-L42)

---

## Testing the Pattern

### Test Entity Creation with Sequence

**See Test Files**:
- [AlbumRepositoryTest.java](../../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/AlbumRepositoryTest.java)
- [TrackRepositoryTest.java](../../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/TrackRepositoryTest.java)
- [ArtistRepositoryTest.java](../../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/ArtistRepositoryTest.java)

**Testing Pattern**:
```java
@Test
void createEntity_shouldGenerateSequentialId() {
    Entity entity1 = repository.save(createEntity("Test 1"));
    Entity entity2 = repository.save(createEntity("Test 2"));

    assertThat(entity1.getId()).isNotNull();
    assertThat(entity2.getId()).isNotNull();
    // IDs may not be sequential with allocationSize > 1
}
```

---

## Troubleshooting

### Problem: Sequence Out of Sync

**Symptom**: Primary key violation errors on insert

**Cause**: Sequence value is lower than actual max ID in table

**Solution**: Reset sequence to max ID + 1

```sql
-- Find current max ID
SELECT MAX(id) FROM {table};

-- Reset sequence (replace {max_id} with actual max + 1)
SELECT setval('{table}_seq', {max_id}, false);
```

---

### Problem: Large Gaps in IDs

**Symptom**: IDs jump by 50 or more between records

**Cause**: High `allocationSize` with frequent application restarts

**Solution**:
- If gaps are acceptable: No action needed
- If sequential IDs required: Reduce `allocationSize` to 1

---

### Problem: Hibernate Doesn't Use Sequence

**Symptom**: IDs are null or not generated

**Cause**: Missing `@GeneratedValue` or incorrect generator name

**Solution**: Verify annotations match:
```java
@SequenceGenerator(name = "my_seq_gen", ...)  // ← Name here
@GeneratedValue(generator = "my_seq_gen")     // ← Must match
```

---

## Allocation Size Guidelines

| Entity Type | Creation Pattern | Recommended allocationSize | Example |
|-------------|-----------------|---------------------------|---------|
| Master data | Batch import | 50 | Album, Track |
| Quiz entities | On-demand | 1 | Game, Generation |
| Raw data (API import) | Batch import | 50 | LastfmArtist, LastfmAlbum |
| Binding entities | One-by-one | 1 | ArtistBinding |
| Historical data | Bulk load | 100 | AttributeHistory |

---

## Related Patterns

- **[BaseEntity Pattern](base-entity.md)** - All entities extend BaseEntity
- **[Entity Naming Conventions](entity-naming-conventions.md)** - Sequence naming standards
- **[Liquibase Pattern](../database/liquibase.md)** - Creating sequences in migrations
- **[Testing Patterns](../testing/testing-with-persistence-layer.md)** - Testing sequence generation

---

## Examples in Codebase

### Entity Files

**Batch Entities (allocationSize = 50)**:
- [Album.java](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java)
- [Track.java](../../../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Track.java)
- [LastfmArtist.java](../../../../../music/data/raw/lastfm/lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/entity/LastfmArtist.java)

**On-Demand Entities (allocationSize = 1)**:
- [Game.java](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/Game.java)
- [Generation.java](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/Generation.java)

### Migration Files

**Sequence Creation**:
- [0002-0010-album$initial.sql](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changesets/0002-album/0002-0010-album$initial.sql)
- [0004-0010-track$initial.sql](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changesets/0004-track/0004-0010-track$initial.sql)

**Changelog Index**:
- [0002-album.xml](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changelogs/0002-album.xml)
- [0004-track.xml](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changelogs/0004-track.xml)

### Test Files

**Repository Tests**:
- [AlbumRepositoryTest.java](../../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/AlbumRepositoryTest.java)
- [TrackRepositoryTest.java](../../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/TrackRepositoryTest.java)
- [ArtistRepositoryTest.java](../../../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/ArtistRepositoryTest.java)
