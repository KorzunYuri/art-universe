# Context Factory Pattern

## What It Is

Factory pattern for creating lookup context objects based on entity type and state, abstracting context creation logic.

## Why It Exists

Centralizes context creation logic, enables automatic context derivation from entities, and supports extensible context types.

## Factories

### LookupContextFactory

Location: [src/shared/types/lookup-context.ts](../../../src/shared/types/lookup-context.ts)

**Purpose:** Create basic lookup contexts

**Methods:**
- `basic()`: Returns `{ type: 'basic' }`

**Usage:**
- Default context for unscoped lookups
- Fallback when no special scoping needed

### RawEntityLookupContextFactory

Location: [src/music/data/raw/shared/types/lookup-context.ts](../../../src/music/data/raw/shared/types/lookup-context.ts)

**Purpose:** Create contexts from raw entities

**Methods:**
- `fromRawEntity<T>(entity: RawEntity<T>)`: Derives context from entity

**Logic:**
1. Check if entity is artist-related using type guard
2. If artist-related: Extract artist IDs and data source
3. Create artist-related context
4. If not artist-related: Return basic context

**Usage:**
- Automatic context in binding workflow
- Scoped lookups based on entity relationships

## Context Derivation

### For Artist

**Input:** Artist entity

**Output:** Basic context (no scoping)

**Reason:** Artists are top-level, no parent to scope by

### For Album

**Input:** Album entity (raw)

**Output:** Artist-related context

**Fields:**
- `dataSource`: From entity `getDataSource()`
- `externalArtistId`: From entity `getExternalArtistId()`
- `masterArtistId`: From entity `getMasterArtistId()` (if bound)

**Effect:** Album lookups scoped to album's artist

### For Track

**Input:** Track entity (raw)

**Output:** Artist-related context

**Fields:**
- Same as album
- Scopes track lookups to track's artist

### For Category/Tag

**Input:** Category/Tag entity

**Output:** Basic context

**Reason:** Categories are top-level or hierarchical, not artist-related

## Type Safety

**Generic Type Parameter:**
- `<T extends MasterEntityType>`
- Ensures entity type validity

**Type Guards:**
- Runtime check for artist-related entities
- Type narrowing for safe property access

**Return Type:**
- Union of possible context types
- Type discriminated by `type` field

## Factory Benefits

**Encapsulation:**
- Context creation logic in one place
- Components don't need to know context structure

**Consistency:**
- Same context for same entity type
- Predictable behavior across components

**Extensibility:**
- Add new context types without changing call sites
- New factories for new domain modules

## Usage in Components

### EntityBinding Component

Location: [EntityBinding.tsx](../../../src/music/data/raw/shared/components/EntityBinding/EntityBinding.tsx)

**Pattern:**
- Get raw entity
- Call `RawEntityLookupContextFactory.fromRawEntity(entity)`
- Pass context to EntityLookup
- Lookup automatically scoped

**Benefit:** Component doesn't know context logic, just calls factory

### EntityLookup Component

Location: [EntityLookup.tsx](../../../src/shared/components/EntityLookup/EntityLookup.tsx)

**Pattern:**
- Accept context prop
- Default to `LookupContextFactory.basic()` if not provided
- Pass context to lookup hook

**Benefit:** Flexible - works with any context type

## Related Patterns

- [Lookup Context](./lookup-context.md) - Context types
- [Type Guards](../data-types/type-guards.md) - Used in factories
- [Lookup Registry](./lookup-registry.md) - Consumes contexts

## Related Documentation

- [Binding Workflow](../../flows/binding-workflow.md) - Factory usage in binding
