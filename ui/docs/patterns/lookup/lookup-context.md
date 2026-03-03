# Lookup Context Pattern

## What It Is

Context system for providing additional parameters to lookup queries, enabling scoped lookups (e.g., albums for specific artist).

## Why It Exists

Allows lookups to be filtered by related entities without coupling components to specific API parameters, enables flexible lookup scoping, and supports artist-related entity lookups.

## Context Types

### Basic Context

Location: [src/shared/types/lookup-context.ts](../../../src/shared/types/lookup-context.ts)

**Purpose:** No scoping, search all entities

**Structure:**
- `type`: 'basic'

**Use Cases:**
- Artist lookup
- Category lookup
- Any entity without parent relation

### Artist-Related Context

Location: [src/music/data/raw/shared/types/lookup-context.ts](../../../src/music/data/raw/shared/types/lookup-context.ts)

**Purpose:** Scope lookup to specific artist

**Structure:**
- `type`: 'artist-related'
- `dataSource`: Optional data source ('lastfm', etc.)
- `externalArtistId`: Optional raw artist ID
- `masterArtistId`: Optional master artist ID

**Use Cases:**
- Album lookup scoped to artist
- Track lookup scoped to artist

## Context Factories

### LookupContextFactory

Location: [src/shared/types/lookup-context.ts](../../../src/shared/types/lookup-context.ts)

**Methods:**
- `basic()`: Creates basic context

**Usage:**
- Default context when no scoping needed
- Fallback when entity type doesn't support scoping

### RawEntityLookupContextFactory

Location: [src/music/data/raw/shared/types/lookup-context.ts](../../../src/music/data/raw/shared/types/lookup-context.ts)

**Methods:**
- `fromRawEntity<T>(entity)`: Creates context from raw entity

**Logic:**
1. Check if entity is artist-related (using type guard)
2. If yes: Extract artist IDs and data source
3. If no: Return basic context

**Usage:**
- Automatic context creation in binding workflow
- Context derived from entity being bound

## Context Usage Flow

### In Binding Workflow

**Process:**
1. User binds raw album to master album
2. Component calls `RawEntityLookupContextFactory.fromRawEntity(rawAlbum)`
3. Factory extracts artist IDs from album
4. Context passed to album lookup
5. Lookup scoped to that artist's albums

### In Lookup Registry

**Process:**
1. Component provides context with params
2. LookupRegistry calls `transformParams`
3. Transformer extracts context fields
4. Context fields added to API request
5. Backend filters results by context

## Parameter Transformation

### Basic Lookup Transformation

**Input Params:**
- `search`: Search term
- `context`: Basic context
- `limit`: Max results

**Output Request:**
- `search`: Search term
- `limit`: Max results

**Context Impact:** None (basic context adds no fields)

### Artist-Related Transformation

**Input Params:**
- `search`: Search term
- `context`: Artist-related context
- `limit`: Max results

**Output Request:**
- `search`: Search term
- `limit`: Max results
- `masterArtistId`: From context (if present)
- `externalArtistId`: From context (if present)
- `dataSource`: From context (if present)

**Context Impact:** Adds artist scoping fields to request

## Context in Components

### EntityLookup Component

Location: [EntityLookup.tsx](../../../src/shared/components/EntityLookup/EntityLookup.tsx)

**Props:**
- `context`: Optional lookup context
- Defaults to basic context if not provided

**Usage:**
- Binding components provide context from entity
- Standalone lookups use basic context

### EntityBinding Component

Location: [EntityBinding.tsx](../../../src/music/data/raw/shared/components/EntityBinding/EntityBinding.tsx)

**Context Creation:**
- Calls `RawEntityLookupContextFactory.fromRawEntity()`
- Passes context to EntityLookup
- Lookups automatically scoped to entity's relations

## Type Safety

**Context Type Union:**
- `BasicLookupContext | ArtistRelatedLookupContext`
- Type discriminated by `type` field

**Type Guards:**
- Check `context.type` to narrow type
- Access type-specific fields safely

## Extensibility

**Adding New Context Types:**
1. Define new context interface with `type` field
2. Add factory method to appropriate factory
3. Update context union type
4. Update transformers in lookup registry

## Related Patterns

- [Lookup Registry](./lookup-registry.md) - Uses contexts in lookups
- [Context Factory](./context-factory.md) - Factory pattern details
- [Type Guards](../data-types/type-guards.md) - Runtime type checking for contexts

## Related Documentation

- [Entity Types](../data-types/entity-types.md) - Entity type system
- [Binding Workflow](../../flows/binding-workflow.md) - Context usage in binding
