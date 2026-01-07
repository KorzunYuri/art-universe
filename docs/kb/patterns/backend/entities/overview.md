# Backend Entity Patterns - Index

## Overview

All backend entities in Art Universe follow consistent patterns for auditing, enum storage, and database mapping. This document serves as an index to all entity-related patterns with quick reference information.

**Use this when**: Creating new entities, understanding entity design conventions, or applying standard entity patterns.

---

## Available Patterns

- [BaseEntity](base-entity.md) - All entities
- [Coded Enum](coded-enums.md) - Status/Type fields
- [Entity Naming](entity-naming-conventions.md) - Class/table/column names
- [Sequences](sequence-generation.md) - ID generation
- [Validation](validation-patterns.md) - Data integrity
- [Foreign Key + Relationship](foreign-key-with-relationship.md) - ID access + navigation

---

## Patterns Summary

### 1. BaseEntity Pattern

**File**: [base-entity.md](base-entity.md) *(complete)*

**Summary**: All JPA entities extend `BaseEntity` to inherit audit timestamp fields. Each entity defines its own `@Id` field with dedicated sequence.

**Provides**:
- `createdAt` (Instant, auto-populated via @Builder.Default)
- `updatedAt` (Instant, manually updated via setter)
- `@SuperBuilder` support for builder pattern inheritance

**Note**: BaseEntity does NOT contain an `id` field - each entity defines its own

**When to use**: Every JPA entity without exception

**Modules using this**: All modules with JPA entities

---

### 2. Coded Enum Pattern

**File**: [coded-enums.md](coded-enums.md) *(complete example)*

**Summary**: Enums implement the `Coded` interface to store as integers in the database while maintaining type safety in code.

**Provides**:
- Integer storage in database
- Type-safe enum in Java code
- Automatic conversion via `CodedConverter`
- Enum registry for validation

**When to use**:
- Any status field (PENDING, APPROVED, DECLINED)
- Any type field (ARTIST, ALBUM, TRACK)
- Any field that should be an integer in DB but enum in code

**Modules using this**:
- `music:data:master` - ApprovalStatus
- `music:data:raw:lastfm` - ApiCallStatus, ApiCallGenerationStatus
- `music:quiz` - PipelineExecutionStatus

---

### 3. Entity Naming Conventions

**File**: [entity-naming-conventions.md](entity-naming-conventions.md) *(complete)*

**Summary**: Consistent naming standards for JPA entities, database tables, and columns to ensure readability and adherence to Java and SQL conventions.

**When to use**: Creating new entities, designing schemas, code reviews

---

### 4. Sequence Generation Pattern

**File**: [sequence-generation.md](sequence-generation.md) *(complete)*

**Summary**: Consistent pattern for generating primary key IDs using database sequences with proper allocation size configuration.

**When to use**: Creating any new JPA entity, writing migrations

---

### 5. Validation Patterns

**File**: [validation-patterns.md](validation-patterns.md) *(complete)*

**Summary**: Two-layer validation approach using Java Bean Validation (application layer) and database constraints (storage layer).

**When to use**: Adding data constraints to entities, ensuring data integrity

---

### 6. Foreign Key with Read-Only Relationship

**File**: [foreign-key-with-relationship.md](foreign-key-with-relationship.md) *(complete)*

**Summary**: Map both a foreign key ID field and a read-only entity relationship to the same database column for direct ID manipulation with relationship navigation.

**Provides**:
- Explicit Long field for foreign key (direct manipulation)
- Read-only @ManyToOne relationship (convenient navigation)
- Performance optimization (avoid unnecessary queries)
- Support for optional and self-referencing relationships

**When to use**: Entities with foreign keys where you need both ID access and relationship navigation (Album → Artist, Track → Artist)

---

## Related Patterns

### Database Patterns
- **[Database Patterns Overview](../database/overview.md)** - Schema design, migrations, indexes
- **[Liquibase Migration Pattern](../database/liquibase.md)** - Migration file structure

### API Patterns
- **[API Conventions](../api/conventions.md)** - Standard CRUD operations and REST endpoints

### Testing Patterns
- **[Testing Patterns Overview](../testing/overview.md)** - Testing strategy
- **jpa-tests.md** *(planned)* - Repository and persistence tests

---

## Next Steps

- For creating a new entity: See [Entity Naming Conventions](entity-naming-conventions.md), [Sequence Generation](sequence-generation.md), and [Validation Patterns](validation-patterns.md)
- For understanding Coded enums: [Coded Enum Pattern](coded-enums.md)
- For mapping foreign keys: [Foreign Key with Read-Only Relationship](foreign-key-with-relationship.md)
- For module-specific details: See module documentation in [Project Modules Index](../../../../MODULES.md)
