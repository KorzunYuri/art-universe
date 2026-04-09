# Entity Naming Conventions

## Purpose

Establish consistent naming standards for JPA entities, database tables, and columns across the Art Universe project to ensure readability, maintainability, and adherence to Java and SQL conventions.

## When to Use

- Creating new JPA entities
- Designing database schemas
- Mapping entity fields to database columns
- Reviewing entity code for consistency

---

## Naming Standards

### Class Names

**Format**: Singular, PascalCase

**Rules**:
- No prefix/suffix (e.g., `Artist`, not `ArtistEntity`)
- Domain-focused names that reflect business concepts
- Singular form only

**Examples**:
```java
@Entity
public class Artist extends BaseEntity { }

@Entity
public class Album extends BaseEntity { }

@Entity
public class Track extends BaseEntity { }

@Entity
public class Category extends BaseEntity { }
```

**Bad Examples**:
```java
@Entity
public class ArtistEntity { }  // ❌ Don't use suffix

@Entity
public class Artists { }        // ❌ Don't use plural

@Entity
public class TblArtist { }      // ❌ Don't use prefix
```

---

### Table Names

**Format**: Derived from class name, snake_case

**Rules**:
- Singular form
- Lowercase with underscores
- JPA automatically converts PascalCase to snake_case

**Examples**:

| Entity Class | Table Name |
|--------------|------------|
| `Artist` | `artist` |
| `Album` | `album` |
| `TrackBinding` | `track_binding` |
| `LastfmArtist` | `lastfm_artist` |
| `ApiCallStatus` | `api_call_status` |

**Explicit Mapping** (when needed):
```java
@Entity(name = "artist")  // Explicit table name
public class Artist extends BaseEntity { }
```

---

### Column Names

**Format**: snake_case in database, camelCase in Java

**Rules**:
- JPA automatically converts camelCase field names to snake_case columns
- Use descriptive names that reflect the data stored
- No abbreviations unless widely understood

**Automatic Conversion Examples**:

| Java Field | Database Column |
|------------|-----------------|
| `name` | `name` |
| `approvalStatus` | `approval_status` |
| `createdAt` | `created_at` |
| `updatedAt` | `updated_at` |
| `lastfmId` | `lastfm_id` |
| `playCount` | `play_count` |

**Code Example**:
```java
@Entity
public class Artist extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;  // → name

    @Convert(converter = ApprovalStatusConverter.class)
    private ApprovalStatus approvalStatus;  // → approval_status

    @Column(name = "external_id")  // Explicit mapping if needed
    private String externalId;  // → external_id
}
```

---

### Foreign Key Column Names

**Format**: `{referenced_table}_id`

**Rules**:
- Use singular form of referenced table
- Add `_id` suffix
- JPA automatically generates based on field name

**Examples**:

| Java Field | Foreign Key Column |
|------------|-------------------|
| `private Artist artist;` | `artist_id` |
| `private Album album;` | `album_id` |
| `private Category category;` | `category_id` |

**Code Example**:
```java
@Entity
public class Track extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;  // → artist_id foreign key

    @ManyToOne(optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;  // → album_id foreign key
}
```

---

### Binding Entity Names

**Format**: `{EntityA}Binding` or `{EntityA}{EntityB}Binding`

**Purpose**: Entities that connect master data to raw/external data sources

**Examples**:

| Entity Class | Purpose |
|--------------|---------|
| `ArtistBinding` | Binds master Artist to external artist (e.g., LastfmArtist) |
| `AlbumBinding` | Binds master Album to external album |
| `TrackBinding` | Binds master Track to external track |

**Code Example**:
```java
@Entity
public class ArtistBinding extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;  // Master entity

    @ManyToOne(optional = false)
    @JoinColumn(name = "lastfm_artist_id", nullable = false)
    private LastfmArtist lastfmArtist;  // External entity

    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;
}
```

---

### Enum Field Names

**Format**: Descriptive noun indicating what the enum represents

**Examples**:

| Field Name | Enum Type | Purpose |
|------------|-----------|---------|
| `approvalStatus` | `ApprovalStatus` | PENDING, APPROVED, DECLINED |
| `dataSource` | `DataSource` | LASTFM, SPOTIFY, etc. |
| `executionStatus` | `PipelineExecutionStatus` | RUNNING, COMPLETED, FAILED |

**Code Example**:
```java
@Entity
public class Artist extends BaseEntity {

    @Convert(converter = ApprovalStatusConverter.class)
    private ApprovalStatus approvalStatus;  // → approval_status (SMALLINT)
}
```

---

## Common Patterns

### Timestamp Fields

Standard names from BaseEntity:
- `createdAt` → `created_at`
- `updatedAt` → `updated_at`

### ID Fields

- Always named `id` in Java
- Always `BIGINT` in database
- Use dedicated sequence per entity

### Boolean Fields

**Format**: Use descriptive names without `is` prefix in field name

**Examples**:

| Java Field | Column Name | Meaning |
|------------|-------------|---------|
| `approved` | `approved` | Is entity approved? |
| `deleted` | `deleted` | Is entity soft-deleted? |
| `active` | `active` | Is entity active? |

```java
@Column(nullable = false)
private Boolean approved = false;  // → approved (BOOLEAN)
```

---

## Examples from Codebase

### Simple Entity
`music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Artist.java`

### Binding Entity
`music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/binding/ArtistBinding.java`

### Raw Data Entity
`music/data/raw/lastfm/lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/entity/LastfmArtist.java`

---

## Related Patterns

- **[BaseEntity Pattern](base-entity.md)** - Base class for all entities
- **[Coded Enum Pattern](coded-enums.md)** - Enum storage conventions
- **[Sequence Generation](sequence-generation.md)** - ID generation naming
- **[Database Patterns](../database/overview.md)** - Schema design conventions
- **[Liquibase Pattern](../database/liquibase.md)** - Migration file naming

---

## Validation Checklist

When creating a new entity, verify:

- [ ] Entity class name is singular PascalCase
- [ ] No `Entity` suffix on class name
- [ ] Table name matches entity (snake_case)
- [ ] All fields use camelCase in Java
- [ ] Foreign keys follow `{table}_id` pattern
- [ ] Enum fields have descriptive names
- [ ] Boolean fields don't use `is` prefix
- [ ] Follows existing codebase patterns
