# Query Key Structure

## What It Is

Hierarchical query key factories that generate type-safe cache keys for React Query, enabling selective cache invalidation and efficient data management.

## Why It Exists

Provides consistent cache key structure across the application, enables granular cache invalidation, and prevents key collisions between different data types.

## Location

[src/music/shared/utils/query-keys.ts](../../src/music/shared/utils/query-keys.ts)

## Key Factory Pattern

**Hierarchical Structure:** General → Specific

Each level adds specificity, allowing invalidation at any level.

## Three Key Factories

### 1. Raw Entities Keys

**Purpose:** Keys for external raw data (LastFM, etc.)

**Structure:**
- `all`: `['rawEntities']` - All raw entities
- `source`: `['rawEntities', dataSource]` - All entities from data source
- `type`: `['rawEntities', dataSource, entityType]` - All entities of type from source
- `list`: `['rawEntities', dataSource, entityType, params]` - Paginated list
- `detail`: `['rawEntities', dataSource, entityType, 'detail', id]` - Single entity

**Usage:**
- List LastFM artists page 0: `rawEntitiesKeys.list('lastfm', 'artist', { page: 0 })`
- Single LastFM artist: `rawEntitiesKeys.detail('lastfm', 'artist', 123)`

### 2. Master Entities Keys

**Purpose:** Keys for canonical master data

**Structure:**
- `all`: `['masterEntities']` - All master entities
- `type`: `['masterEntities', entityType]` - All entities of type
- `list`: `['masterEntities', entityType, params]` - Paginated list
- `detail`: `['masterEntities', entityType, 'detail', id]` - Single entity
- `withRelations`: `['masterEntities', entityType, 'with-relations', params]` - List with relations
- `withRelationsDetail`: `['masterEntities', entityType, 'with-relations', 'detail', id]` - Single with relations

**Usage:**
- List artists: `masterEntitiesKeys.list('artist', { page: 0 })`
- Single artist: `masterEntitiesKeys.detail('artist', 456)`
- Artist with categories: `masterEntitiesKeys.withRelationsDetail('artist', 456)`

### 3. Entity Lookup Keys

**Purpose:** Keys for autocomplete/lookup queries

**Structure:**
- `all`: `['lookup']` - All lookups
- `dataSource`: `['lookup', dataSource | 'master']` - All lookups for source
- `type`: `['lookup', dataSource | 'master', entityType]` - All lookups of type
- `query`: `['lookup', dataSource | 'master', entityType, params]` - Specific lookup query

**Usage:**
- Lookup master artists: `entityLookupKeys.query('master', 'artist', { search: 'Beatles' })`
- Lookup LastFM tags: `entityLookupKeys.query('lastfm', 'tag', { search: 'rock' })`

## Key Hierarchy Benefits

**Selective Invalidation:**
- Invalidate all artists: `masterEntitiesKeys.type('artist')`
- Invalidate specific artist list: `masterEntitiesKeys.list('artist', { page: 0 })`
- Invalidate single artist: `masterEntitiesKeys.detail('artist', 456)`

**Cache Efficiency:**
- React Query matches keys by prefix
- Broader keys invalidate narrower keys
- Narrower keys preserve unaffected cache

## Type Safety

All key factories use TypeScript `as const` for literal types, ensuring type-safe key generation and preventing runtime errors.

## Const Assertions

Each key factory method returns `as const` to preserve literal types:

- Enables TypeScript to infer exact key structure
- Prevents accidental key modifications
- Ensures key consistency across application

## Parameters in Keys

### Pagination Parameters

Included in list keys:
- `page`: Page number (0-indexed)
- `size`: Page size (optional, default 20)
- `sort`: Sort order (optional, default 'name,asc')
- `search`: Search term (optional)

### Lookup Parameters

Included in lookup keys:
- `search`: Search string
- `limit`: Max results (optional, default 10)
- `context`: Lookup context (basic or artist-related)

## Related Documentation

- [React Query Overview](./README.md) - React Query setup
- [Cache Invalidation](./cache-invalidation.md) - Using keys for invalidation
- [Hook Patterns](./hooks.md) - Using keys in hooks
