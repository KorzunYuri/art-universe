# Entity Validation Patterns

## Purpose

Define consistent validation patterns for JPA entities to ensure data integrity at both the application layer (Java Bean Validation) and database layer (constraints).

## When to Use

- Creating new JPA entities with data constraints
- Adding validation to entity fields
- Ensuring data integrity before persistence
- Preventing invalid data in the database
- Providing meaningful error messages to users

---

## Validation Layers

Art Universe uses **two layers of validation**:

1. **Application Layer** - Java Bean Validation (JSR 303/380)
   - Validates before save
   - Provides user-friendly error messages
   - Faster feedback (no DB round-trip)

2. **Database Layer** - Database constraints
   - Last line of defense
   - Enforces integrity at storage level
   - Protects against direct SQL manipulation

---

## Field-Level Validation

### Required Fields

**Application Layer**: `@NotNull` or `@NonNull` (Lombok)
**Database Layer**: `@Column(nullable = false)`

**Best Practice**: Use both layers for complete validation

**See Examples**:
- [Album.java:25-27](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L25-L27) - Required name field
- [Album.java:29-31](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L29-L31) - Required foreign key
- [Track.java:25-27](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Track.java#L25-L27) - Required name field

**Pattern**:
```java
@NonNull
@Column(name = "field_name", nullable = false)
private String fieldName;
```

---

### String Length Constraints

**Application Layer**: `@Size(min = X, max = Y, message = "...")`
**Database Layer**: `@Column(length = Y)`

**Best Practice**: Match `@Size` max with `@Column` length

**See Database Constraints**:
- [Album migration](../../../../../music/data/master/music-master-rest-api/src/main/resources/db/migration/mu/liquibase/changesets/0002-album/0002-0010-album$initial.sql) - VARCHAR(1024) for name
- [Track migration](../../../../../music/data/master/music-master-rest-api/src/main/resources/db/migration/mu/liquibase/changesets/0004-track/0004-0010-track$initial.sql) - VARCHAR(1024) for name

**Pattern**:
```java
@Size(max = 1024, message = "Name cannot exceed 1024 characters")
@Column(name = "name", length = 1024)
private String name;
```

---

### Email Format Validation

**Application Layer**: `@Email(message = "Invalid email format")`
**Database Layer**: `@Column(length = 255)`

**Pattern**:
```java
@Email(message = "Invalid email format")
@Column(length = 255)
private String contactEmail;
```

---

### Numeric Range Validation

**Application Layer**: `@Min(value = X)`, `@Max(value = Y)`
**Database Layer**: Database CHECK constraint

**Pattern**:
```java
@Min(value = 1900, message = "Release year must be 1900 or later")
@Max(value = 2100, message = "Release year must be 2100 or earlier")
@Column(name = "release_year")
private Integer releaseYear;
```

---

### Regular Expression Validation

**Application Layer**: `@Pattern(regexp = "...", message = "...")`

**Pattern**:
```java
@Pattern(
    regexp = "^[A-Z]{2}-[A-Z0-9]{3}-\\d{2}-\\d{5}$",
    message = "Invalid ISRC code format"
)
@Column(length = 15)
private String isrcCode;
```

---

## Entity-Level Validation

### Database Check Constraints

**Use when**: Validation involves multiple fields or complex logic beyond simple NOT NULL

**See Migration Examples**:
- [Album migration](../../../../../music/data/master/music-master-rest-api/src/main/resources/db/migration/mu/liquibase/changesets/0002-album/0002-0010-album$initial.sql) - Foreign key constraints

**Pattern**:
```java
@Entity
@Check(constraints = "approval_status IN (1, 2, 3, 4)")
public class Artist extends BaseEntity {
    // ...
}
```

**Named Constraints (Recommended)**:
```java
@Entity
@Table(name = "album")
@Check(
    name = "album_year_range",
    constraints = "release_year >= 1900 AND release_year <= 2100"
)
public class Album extends BaseEntity {
    // ...
}
```

---

### Multi-Field Validation with @AssertTrue

**Use when**: Validation logic spans multiple fields

**Pattern**:
```java
@AssertTrue(message = "Reissue year must be after release year")
public boolean isReissueYearValid() {
    if (releaseYear == null || reissueYear == null) {
        return true;  // Skip validation if either is null
    }
    return reissueYear >= releaseYear;
}
```

---

## Common Validation Patterns

### Required String Field with Length

**See**: [Album.java:25-27](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L25-L27)

```java
@NonNull
@Column(name = "name", nullable = false, length = 1024)
private String name;
```

**With Java Bean Validation**:
```java
@NotNull(message = "Name is required")
@Size(min = 1, max = 1024, message = "Name must be 1-1024 characters")
@Column(name = "name", nullable = false, length = 1024)
private String name;
```

---

### Optional String Field with Max Length

```java
@Size(max = 1000, message = "Description cannot exceed 1000 characters")
@Column(length = 1000)
private String description;
```

---

### Required Foreign Key

**See**: [Album.java:29-31](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java#L29-L31)

```java
@NonNull
@Column(name = "primary_artist_id", nullable = false)
private Long primaryArtistId;
```

**With Java Bean Validation**:
```java
@NotNull(message = "Artist is required")
@Column(name = "primary_artist_id", nullable = false)
private Long primaryArtistId;
```

---

### Optional Numeric Field with Range

```java
@Min(value = 1900, message = "Year must be 1900 or later")
@Max(value = 2100, message = "Year must be 2100 or earlier")
@Column(name = "release_year")
private Integer releaseYear;  // Nullable
```

---

### Boolean Field with Default

**Pattern**:
```java
@NonNull
@Column(nullable = false)
@Builder.Default
private Boolean approved = false;
```

**Note**: No validation message needed - defaults handle null case

---

## Testing Validation

### Test Required Fields

**See Test Examples**:
- [AlbumRepositoryTest.java](../../../../../music/data/master/music-master-rest-api/src/test/java/yurykorzun/art/universe/music/data/master/repository/AlbumRepositoryTest.java)
- [TrackRepositoryTest.java](../../../../../music/data/master/music-master-rest-api/src/test/java/yurykorzun/art/universe/music/data/master/repository/TrackRepositoryTest.java)

**Pattern**:
```java
@Test
void createEntity_shouldFail_whenRequiredFieldIsNull() {
    Entity entity = Entity.builder()
        .requiredField(null)  // ❌ Violates @NotNull or @NonNull
        .build();

    assertThrows(Exception.class, () -> {
        repository.save(entity);
    });
}
```

---

### Test String Length Constraints

```java
@Test
void createEntity_shouldFail_whenNameTooLong() {
    String longName = "A".repeat(1025);  // Exceeds 1024 chars

    Entity entity = Entity.builder()
        .name(longName)  // ❌ Violates length constraint
        .build();

    assertThrows(Exception.class, () -> {
        repository.save(entity);
    });
}
```

---

### Test Database Constraints

```java
@Test
void createEntity_shouldFail_whenForeignKeyInvalid() {
    Entity entity = Entity.builder()
        .name("Test")
        .foreignKeyId(99999L)  // ❌ Non-existent FK
        .build();

    assertThrows(Exception.class, () -> {
        repository.save(entity);
    });
}
```

---

## Validation Best Practices

### 1. Always Pair Validation Layers

```java
// ✅ Good: Application + Database validation
@NotNull(message = "Name is required")
@Column(nullable = false)
private String name;

// ❌ Bad: Only application validation
@NotNull(message = "Name is required")
private String name;

// ❌ Bad: Only database validation
@Column(nullable = false)
private String name;
```

---

### 2. Provide Clear Error Messages

```java
// ✅ Good: Specific, actionable message
@Size(min = 1, max = 255, message = "Artist name must be 1-255 characters")

// ❌ Bad: Generic message
@Size(min = 1, max = 255, message = "Invalid size")
```

---

### 3. Use Builder Defaults for Required Fields

```java
// ✅ Good: Default value prevents null
@NonNull
@Column(nullable = false)
@Builder.Default
private Boolean approved = false;

// ❌ Bad: No default, easy to forget
@NonNull
@Column(nullable = false)
private Boolean approved;
```

---

### 4. Test Validation Rules

Every validation constraint should have a corresponding test:
- Test that valid data passes
- Test that invalid data fails
- Verify error messages are correct

**See**: Repository test files for examples

---

## Related Patterns

- **[BaseEntity Pattern](base-entity.md)** - All entities extend BaseEntity
- **[Coded Enum Pattern](coded-enums.md)** - Validating enum values
- **[Entity Naming Conventions](entity-naming-conventions.md)** - Field naming for validation
- **[Foreign Key with Relationship](foreign-key-with-relationship.md)** - Validating foreign keys
- **[Testing Patterns](../testing/testing-with-persistence-layer.md)** - Testing validation

---

## Examples in Codebase

### Entities with Database Validation

- [Album.java](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java) - Required fields with @NonNull and @Column(nullable = false)
- [Track.java](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Track.java) - Required fields and foreign keys

### Migration Files with Constraints

- [Album migration](../../../../../music/data/master/music-master-rest-api/src/main/resources/db/migration/mu/liquibase/changesets/0002-album/0002-0010-album$initial.sql) - NOT NULL, foreign keys
- [Track migration](../../../../../music/data/master/music-master-rest-api/src/main/resources/db/migration/mu/liquibase/changesets/0004-track/0004-0010-track$initial.sql) - NOT NULL, foreign keys

### Validation Tests

- [AlbumRepositoryTest.java](../../../../../music/data/master/music-master-rest-api/src/test/java/yurykorzun/art/universe/music/data/master/repository/AlbumRepositoryTest.java)
- [TrackRepositoryTest.java](../../../../../music/data/master/music-master-rest-api/src/test/java/yurykorzun/art/universe/music/data/master/repository/TrackRepositoryTest.java)
- [ArtistRepositoryTest.java](../../../../../music/data/master/music-master-rest-api/src/test/java/yurykorzun/art/universe/music/data/master/repository/ArtistRepositoryTest.java)

---

## Implementation Status

**Current**: Most entities use `@NonNull` (Lombok) and database-level `@Column(nullable = false)` constraints

**Future Enhancement**: Add Java Bean Validation annotations (@NotNull, @Size, @Email, etc.) for better application-level validation and error messages
