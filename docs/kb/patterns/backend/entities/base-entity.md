# BaseEntity Pattern

## Purpose

Provide base class for all JPA entities in Art Universe. 

**Important**: BaseEntity does NOT contain an `id` field - each entity defines its own ID with a dedicated sequence generator.

## When to Use

Use this pattern for:

- **All JPA entities** - Every entity must extend BaseEntity
- **Entities requiring audit timestamps** - Track creation and modification times
- **Entities using builder pattern** - @SuperBuilder enables builders in subclasses

Do NOT use for:

- **DTOs** - Data Transfer Objects don't need persistence
- **Projections** - Query result classes
- **Non-persistent classes** - Value objects, utility classes


## Key Concept

**Class**: [BaseEntity](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/persistence/entity/BaseEntity.java)

`BaseEntity` is a JPA `@MappedSuperclass` that provides only timestamp fields. Each entity defines its own `@Id` field with a dedicated sequence generator.

`BaseEntity` standardizes entity structure and enables builder pattern inheritance via Lombok's `@SuperBuilder`.

**Why Art Universe does it this way**:
- **No ID in BaseEntity**: Each entity needs its own sequence (artist_seq, category_seq, etc.)
- **@Builder.Default with Instant.now()**: Simple timestamp initialization without @EnableJpaAuditing configuration
- **@SuperBuilder**: Enables fluent builder pattern in all entity subclasses
- **Manual updatedAt control**: Setter allows manual updates when needed


## Implementation Steps

### Step 1: Extend BaseEntity

Create your entity extending BaseEntity. 

**Key Points**:
- Use `@SuperBuilder` (not @Builder) to inherit builder pattern
- Include `@NoArgsConstructor` - JPA requires default constructor
- BaseEntity provides `createdAt` and `updatedAt` automatically
- Use `@Getter` and `@Setter` for field access


### Step 2: Define Entity's Own ID with Sequence

Every entity MUST define its own `@Id` field with a dedicated sequence generator.

**Key Points**:
- **Each entity has unique sequence**: `artist_seq`, `category_seq`, `album_seq`
- **allocationSize**: Set to 50 for performance (batch allocation)
- **@Setter(AccessLevel.NONE)**: ID is managed by JPA, not settable
- **Database sequence must exist**: Created in Liquibase migration

**Why ID is NOT in BaseEntity**:
- Each entity needs a different sequence name
- Cannot parameterize @SequenceGenerator in parent class
- Allows flexibility for composite keys if needed


### Step 3: Add Business Fields

Add your entity-specific fields.

**Key Points**:
- `createdAt` and `updatedAt` are inherited - no code needed
- Add validation annotations as needed (@NonNull, @NotNull)
- Define relationships using standard JPA annotations


## Database Schema

### Table Definition

```sql
CREATE SEQUENCE artist_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE artist (
    id BIGINT NOT NULL DEFAULT nextval('artist_seq'),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,    -- From BaseEntity
    updated_at TIMESTAMP NOT NULL,    -- From BaseEntity
    PRIMARY KEY (id)
);
```

**Key Points**:
- **Sequence created first** with matching INCREMENT BY (allocationSize)
- **created_at/updated_at**: Columns inherited from BaseEntity
- **TIMESTAMP type**: Maps to Java Instant
- **NOT NULL**: Timestamps always populated via @Builder.Default


## Examples in Art Universe Codebase

- [Master Artist](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Artist.java) - Simple Entity with Relationships
- [Master Category](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/Category.java) - Entity with Bidirectional Relationships
- [Master Artsit Binding](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/ArtistBinding.java) - Binding Entity


## Best Practices

**Art Universe Standards**:
- ✅ **Always extend BaseEntity** for JPA entities
- ✅ **Always define @Id** with dedicated sequence in entity class
- ✅ **Use @SuperBuilder** in entity classes (not @Builder)
- ✅ **Include @NoArgsConstructor** for JPA requirement
- ✅ **Manually update updatedAt** when modifying entities: `entity.setUpdatedAt(Instant.now())`
- ✅ **Set allocationSize to 50** for performance optimization
- ✅ **Name sequences after entity**: artist_seq, category_seq, etc.

**Common Mistakes to Avoid**:
- ❌ **Using @Builder instead of @SuperBuilder** - Builder won't include parent fields
- ❌ **Forgetting to define @Id field** - BaseEntity doesn't provide it
- ❌ **Trying to put @Id in BaseEntity** - Impossible due to sequence naming
- ❌ **Missing @NoArgsConstructor** - JPA requires default constructor
- ❌ **Expecting automatic updatedAt updates** - Must set manually when needed
- ❌ **Wrong allocationSize** - Should match database sequence INCREMENT BY


## Testing

- Test Timestamp Population
- Test Timestamp Immutability
- Test Builder Pattern


## Design Decisions

### Why BaseEntity does NOT contain ID

**Decision**: Each entity defines its own `@Id` field with dedicated sequence

**Reasons**:
1. **Sequence naming**: Each entity needs unique sequence name (artist_seq, category_seq)
2. **@SequenceGenerator limitation**: Cannot parameterize sequenceName in parent class
3. **Flexibility**: Allows entities to use composite keys or other ID strategies if needed
4. **Clarity**: Makes ID generation strategy explicit in each entity

**Alternative considered**: Abstract method to provide sequence name - rejected as overly complex


### Why @Builder.Default instead of JPA Auditing

**Decision**: Use Lombok's `@Builder.Default` with `Instant.now()` for timestamp initialization

**Reasons**:
1. **Simplicity**: No @EnableJpaAuditing configuration needed
2. **Builder compatibility**: Works seamlessly with @SuperBuilder pattern
3. **Manual control**: Can manually update `updatedAt` when needed via setter
4. **Testing**: Easy to override timestamps in tests via builder

**Trade-off**: Must manually update `updatedAt` - not automatic on entity modification

**Alternative considered**: Spring Data JPA auditing with @CreatedDate/@LastModifiedDate - rejected to avoid additional configuration and dependency on Spring Data JPA auditing


### Why Lombok @SuperBuilder

**Decision**: Use `@SuperBuilder` in both BaseEntity and all subclasses

**Reasons**:
1. **Builder inheritance**: Enables fluent builders in subclasses with parent fields
2. **Immutability option**: Can make entities immutable if needed (with @Setter on specific fields)
3. **Test data creation**: Simplifies test entity creation
4. **Consistency**: Uniform entity creation pattern across codebase

**Note**: Requires @SuperBuilder in BOTH parent and child classes to work


## Related Patterns

This pattern is used with:
- [Entity Patterns Overview](overview.md) - Index of all entity patterns
- [Coded Enum Pattern](coded-enums.md) - Type-safe enum fields in entities
- [Sequence Generation Pattern](overview.md#sequence-generation-pattern) - ID generation strategy


## Used In

**All modules with JPA entities**:
- [Music Data Master Module](../../modules/mu-data-master/README.md) - Artist, Album, Track, Category entities
- Music Data Raw LastFM - LastfmArtist, LastfmAlbum, LastfmTrack entities
- Music Quiz Module - PipelineExecution, QuizQuestion entities

**See Also**:
- [Backend Testing Patterns](../testing/overview.md) - Testing entities and repositories
- [Database Patterns](../database/liquibase-workflow.md) - Creating sequences in migrations
- [Liquibase Workflow](../database/liquibase-workflow.md) - Migration file structure


## Quality Checklist

- [x] Pattern is Art Universe-specific (BaseEntity implementation details)
- [x] Pattern references actual codebase files (Artist.java, Category.java, ArtistBinding.java)
- [x] Pattern shows consistency across multiple entities
- [x] Pattern explains WHY Art Universe does it this way (no ID in BaseEntity, @Builder.Default approach)
- [x] Code examples are minimal and focused
- [x] Complete implementations reference source files
- [x] All cross-references use markdown links
