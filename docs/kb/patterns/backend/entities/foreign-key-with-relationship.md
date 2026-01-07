# Foreign Key with Read-Only Relationship Pattern

## Purpose

Map both a foreign key ID field and a read-only entity relationship to the same database column, enabling direct ID manipulation while preserving relationship navigation capabilities.

## When to Use

- Need to set foreign key by ID without loading the related entity
- Want to avoid N+1 query problems when only ID is needed
- Need convenient entity navigation when full object is required
- Working with entities where the foreign key is frequently set programmatically
- Preventing accidental updates through relationship navigation

---

## The Problem

### Approach 1: Only @ManyToOne Relationship

```java
@Entity
public class Album extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "primary_artist_id", nullable = false)
    private Artist primaryArtist;
}
```

**Problems**:
- Must load Artist entity to set the relationship
- Can't set foreign key by ID directly
- Causes extra database queries when you only need the ID
- Can lead to N+1 query problems

```java
// Requires loading Artist from database
Artist artist = artistRepository.findById(artistId).orElseThrow();
album.setPrimaryArtist(artist);  // ❌ Unnecessary query
```

---

### Approach 2: Only Long Foreign Key Field

```java
@Entity
public class Album extends BaseEntity {

    @Column(name = "primary_artist_id", nullable = false)
    private Long primaryArtistId;
}
```

**Problems**:
- No convenient way to navigate to Artist entity
- Must manually join or load Artist when needed
- Loses type safety for relationship
- No IDE autocomplete for related entity

```java
// Must manually load Artist
Artist artist = artistRepository.findById(album.getPrimaryArtistId()).orElseThrow();
// ❌ Verbose and repetitive
```

---

## The Solution: Hybrid Pattern

Map **both** the ID field and the relationship to the **same column**:

```java
@Entity
public class Album extends BaseEntity {

    // 1. Explicit foreign key field (for direct manipulation)
    @NonNull
    @Column(name = "primary_artist_id", nullable = false)
    private Long primaryArtistId;

    // 2. Read-only relationship (for convenient navigation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_artist_id", insertable = false, updatable = false)
    private Artist primaryArtist;
}
```

**Key Attributes**:
- **Foreign Key Field**: Full control, can set by ID
- **Relationship Field**:
  - `insertable = false` - Prevents INSERT via relationship
  - `updatable = false` - Prevents UPDATE via relationship
  - `fetch = FetchType.LAZY` - Only loads when accessed

---

## Implementation Steps

### Step 1: Define the Foreign Key Column

Create the explicit Long field for the foreign key:

```java
@NonNull
@Column(name = "primary_artist_id", nullable = false)
private Long primaryArtistId;
```

**Naming Convention**: `{entity}Id` in Java → `{entity}_id` in database

---

### Step 2: Define the Read-Only Relationship

Map the relationship to the **same column** with read-only flags:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "primary_artist_id", insertable = false, updatable = false)
private Artist primaryArtist;
```

**Required Attributes**:
- `@JoinColumn(name = "...")` - Must match the column name from Step 1
- `insertable = false` - Prevents JPA from using this field in INSERT
- `updatable = false` - Prevents JPA from using this field in UPDATE
- `fetch = FetchType.LAZY` - Defers loading until accessed

---

### Step 3: Create Database Migration

```sql
CREATE TABLE album (
    id BIGINT NOT NULL,
    name VARCHAR(500) NOT NULL,
    primary_artist_id BIGINT NOT NULL,  -- Single column
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (primary_artist_id) REFERENCES artist(id)
);

CREATE INDEX idx_album_primary_artist_id ON album(primary_artist_id);
```

**Note**: Only **one** database column, mapped to **two** Java fields

---

## Usage Examples

### Setting Foreign Key by ID (Common Case)

```java
// ✅ Efficient: No database query needed
Album album = Album.builder()
    .name("Test Album")
    .primaryArtistId(artistId)  // Set ID directly
    .build();

albumRepository.save(album);
```

**Benefit**: No need to load Artist entity, just set the ID

---

### Navigating to Related Entity (When Needed)

```java
// Load album
Album album = albumRepository.findById(albumId).orElseThrow();

// ✅ Convenient: Access relationship when needed
String artistName = album.getPrimaryArtist().getName();
```

**Benefit**: Relationship navigation when you need the full entity

---

### Getting ID Without Loading Entity

```java
Album album = albumRepository.findById(albumId).orElseThrow();

// ✅ Efficient: No extra query
Long artistId = album.getPrimaryArtistId();  // Just the ID field
```

**Benefit**: Access ID without triggering lazy load

---

### Query Examples

```java
// Query using ID field
List<Album> albums = albumRepository.findByPrimaryArtistId(artistId);

// Query with join using relationship field
@Query("SELECT a FROM album a JOIN FETCH a.primaryArtist WHERE a.id = :id")
Album findByIdWithArtist(@Param("id") Long id);
```

---

## Complete Example

### Entity Class

```java
package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "album")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Album extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "album_seq_gen",
        sequenceName = "album_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name", nullable = false)
    private String name;

    // Pattern: Explicit foreign key field
    @NonNull
    @Column(name = "primary_artist_id", nullable = false)
    private Long primaryArtistId;

    // Pattern: Read-only relationship to same column
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_artist_id", insertable = false, updatable = false)
    private Artist primaryArtist;
}
```

---

### Service Layer Usage

```java
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    // Creating album - use ID field
    public Album createAlbum(String name, Long artistId) {
        Album album = Album.builder()
            .name(name)
            .primaryArtistId(artistId)  // ✅ Set by ID
            .build();

        return albumRepository.save(album);
    }

    // Displaying album - use relationship
    public AlbumDto getAlbumWithArtist(Long albumId) {
        Album album = albumRepository.findByIdWithArtist(albumId);

        return AlbumDto.builder()
            .id(album.getId())
            .name(album.getName())
            .artistName(album.getPrimaryArtist().getName())  // ✅ Navigate relationship
            .build();
    }

    // Listing albums - use ID field
    public List<Album> getAlbumsByArtist(Long artistId) {
        return albumRepository.findByPrimaryArtistId(artistId);  // ✅ Query by ID
    }
}
```

---

## Optional vs Required Relationships

### Required Relationship (Non-Null)

```java
// Required: foreign key must have a value
@NonNull
@Column(name = "primary_artist_id", nullable = false)
private Long primaryArtistId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "primary_artist_id", insertable = false, updatable = false)
private Artist primaryArtist;
```

**Database**: `NOT NULL` constraint

---

### Optional Relationship (Nullable)

```java
// Optional: foreign key can be null
@Column(name = "album_group_id")
private Long albumGroupId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "album_group_id", insertable = false, updatable = false)
private Album originalAlbum;
```

**Database**: No `NOT NULL` constraint

**Use Case**: Album groups (reissues, remasters) - original album is optional

---

## Self-Referencing Relationships

Pattern also works for self-referencing (entity references itself):

```java
@Entity
public class Album extends BaseEntity {

    // Optional: ID of original album (for reissues)
    @Column(name = "album_group_id")
    private Long albumGroupId;

    // Optional: Navigate to original album
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_group_id", insertable = false, updatable = false)
    private Album originalAlbum;
}
```

**Example**:
```java
// Original album
Album original = Album.builder()
    .name("Dark Side of the Moon")
    .primaryArtistId(pinkFloydId)
    .build();

// Reissue references original
Album reissue = Album.builder()
    .name("Dark Side of the Moon (Remastered)")
    .primaryArtistId(pinkFloydId)
    .albumGroupId(original.getId())  // Links to original
    .build();
```

---

## Common Mistakes

### ❌ Mistake 1: Not Marking Relationship as Read-Only

```java
// ❌ Wrong: Missing insertable/updatable flags
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "primary_artist_id")
private Artist primaryArtist;
```

**Problem**: JPA tries to manage the column through both fields → conflicts

---

### ❌ Mistake 2: Different Column Names

```java
// ❌ Wrong: Column names don't match
@Column(name = "primary_artist_id", nullable = false)
private Long primaryArtistId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "artist_id", insertable = false, updatable = false)
private Artist primaryArtist;
```

**Problem**: Maps to different columns, defeats the purpose

---

### ❌ Mistake 3: Using EAGER Fetch

```java
// ❌ Wrong: EAGER fetching
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "primary_artist_id", insertable = false, updatable = false)
private Artist primaryArtist;
```

**Problem**: Loses performance benefit, always loads Artist

---

### ❌ Mistake 4: Setting Relationship Instead of ID

```java
// ❌ Wrong: Setting through relationship
Artist artist = artistRepository.findById(artistId).orElseThrow();
album.setPrimaryArtist(artist);
```

**Problem**: The relationship is read-only, won't be persisted

**Correct**:
```java
// ✅ Correct: Set the ID field
album.setPrimaryArtistId(artistId);
```

---

## Testing the Pattern

### Test Direct ID Assignment

```java
@Test
void createAlbum_shouldSetForeignKeyById() {
    Long artistId = 1L;

    Album album = Album.builder()
        .name("Test Album")
        .primaryArtistId(artistId)  // Set by ID
        .build();

    album = albumRepository.save(album);

    assertThat(album.getPrimaryArtistId()).isEqualTo(artistId);
}
```

---

### Test Relationship Navigation

```java
@Test
void getAlbum_shouldNavigateToArtist() {
    // Given: Album with artist
    Artist artist = createAndSaveArtist("Test Artist");
    Album album = Album.builder()
        .name("Test Album")
        .primaryArtistId(artist.getId())
        .build();
    album = albumRepository.save(album);

    // When: Load and navigate
    Album loaded = albumRepository.findById(album.getId()).orElseThrow();
    String artistName = loaded.getPrimaryArtist().getName();

    // Then: Relationship works
    assertThat(artistName).isEqualTo("Test Artist");
}
```

---

### Test Read-Only Constraint

```java
@Test
void updateAlbum_shouldNotUpdateThroughRelationship() {
    // Given: Album with artist
    Album album = createAlbumWithArtist();

    // When: Try to change through relationship (should not work)
    Artist newArtist = createAndSaveArtist("New Artist");
    album.setPrimaryArtist(newArtist);  // ❌ Read-only, won't persist
    albumRepository.save(album);

    // Then: Foreign key unchanged
    Album reloaded = albumRepository.findById(album.getId()).orElseThrow();
    assertThat(reloaded.getPrimaryArtistId()).isNotEqualTo(newArtist.getId());
}
```

---

## Benefits

### Performance
- ✅ No unnecessary queries when only ID is needed
- ✅ Avoids N+1 query problems
- ✅ LAZY loading for relationships

### Developer Experience
- ✅ Set foreign keys by ID directly
- ✅ Navigate relationships when needed
- ✅ IDE autocomplete for related entities
- ✅ Type-safe relationship access

### Maintainability
- ✅ Clear intent: ID for setting, relationship for navigation
- ✅ Prevents accidental updates through relationships
- ✅ Single source of truth (one database column)

---

## Related Patterns

- **[Entity Naming Conventions](entity-naming-conventions.md)** - Foreign key naming: `{entity}Id`
- **[BaseEntity Pattern](base-entity.md)** - All entities extend BaseEntity
- **[Sequence Generation](sequence-generation.md)** - ID generation for referenced entities
- **[Validation Patterns](validation-patterns.md)** - Validating foreign key fields

---

## Examples in Codebase

### Required Foreign Keys
- `music/data/master/.../entity/Album.java` - `primaryArtistId` → `primaryArtist`
- `music/data/master/.../entity/Track.java` - `primaryArtistId` → `primaryArtist`

### Optional Self-Referencing
- `music/data/master/.../entity/Album.java` - `albumGroupId` → `originalAlbum`
- `music/data/master/.../entity/Track.java` - `trackGroupId` → `originalTrack`

---

## Quick Reference

| Aspect | Implementation |
|--------|----------------|
| **Foreign Key Field** | `@Column(name = "entity_id")` |
| **Relationship Field** | `@ManyToOne(fetch = LAZY)` |
| **Join Column** | `@JoinColumn(name = "entity_id", insertable = false, updatable = false)` |
| **Setting Value** | Use ID field: `setEntityId(id)` |
| **Navigation** | Use relationship: `getEntity().getName()` |
| **Queries** | Use ID field for filtering |
