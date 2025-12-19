# Lookup Registry Pattern

## What It Is

Centralized registry that maps entity types and data sources to lookup functions, enabling type-safe autocomplete functionality across the application.

## Why It Exists

Decouples components from specific API implementations, provides consistent lookup interface, and enables dynamic lookup configuration based on entity type and data source.

## Location

[src/music/shared/services/LookupRegistry.ts](../../src/music/shared/services/LookupRegistry.ts)

## How It Works

### Registration Phase

**When:** Module initialization

**Where:**
- Master data: [registerMasterLookups.ts](../../src/music/data/master/services/registerMasterLookups.ts)
- LastFM: [registerLastfmLookups.ts](../../src/music/data/raw/lastfm/services/registerLastfmLookups.ts)

**Process:**
1. Module calls `LookupRegistry.register()`
2. Provides data source, entity type, and lookup configuration
3. Registry stores configuration in map with composite key

### Lookup Phase

**When:** User types in autocomplete field

**Process:**
1. Component calls `useEntityLookup()` hook
2. Hook calls `LookupRegistry.lookup()`
3. Registry finds configuration by data source + entity type
4. Registry executes lookup function with params
5. Results returned to component

## Key Interfaces

- `LookupConfiguration<TParams, TRequest>`: Configuration for lookup
  - `transformParams`: Converts UI params to API request
  - `lookupEntities`: Executes lookup and returns results
- `LookupEntity`: Base interface for lookup results
- `LookupRegistry`: Singleton registry class

## Composite Key Pattern

**Format:** `{dataSource}:{entityType}`

**Examples:**
- `master:artist`
- `lastfm:artist`
- `master:category`

**Benefits:**
- Unique key per combination
- Type-safe lookup resolution
- Supports multiple data sources for same entity type

## Registration Examples

### Basic Lookup (Artist, Category)

**Pattern:**
- Simple search by name
- No context required
- Returns matching entities

**Configuration:**
- `transformParams`: Maps search + limit to request
- `lookupEntities`: Calls API lookup function

### Context-Based Lookup (Album, Track)

**Pattern:**
- Search scoped by artist context
- Artist ID passed in context
- Returns entities related to artist

**Configuration:**
- `transformParams`: Extracts context (masterArtistId, externalArtistId, dataSource)
- `lookupEntities`: Calls API with context parameters

## Parameter Transformation

**Purpose:** Convert component params to API request format

**Process:**
1. Component provides high-level params (search, context, limit)
2. `transformParams` extracts API-specific fields
3. Returns request object for API function

**Benefits:**
- Components use consistent param interface
- API differences abstracted away
- Type-safe transformation

## Usage in Components

Components use registry through [useEntityLookup](../../src/music/shared/hooks/useEntityLookup.ts) hook.

**Pattern:**
1. Component specifies data source, entity type, params
2. Hook calls registry
3. Component receives results

**Example:** [EntityLookup](../../src/music/shared/components/EntityLookup/EntityLookup.tsx) component

## Singleton Pattern

**Implementation:** Single `LookupRegistry` instance exported

**Benefits:**
- Shared state across application
- Registration happens once
- Lookups access same configuration

## Type Safety

**Generic Type Parameters:**
- `TParams`: Component params type
- `TRequest`: API request type
- `TEntity`: Entity type constraint

**Type Guards:** Runtime validation of entity types

## Related Patterns

- [Lookup Context](./lookup-context.md) - Context system for scoped lookups
- [Context Factory](./context-factory.md) - Factory pattern for creating contexts
- [Entity Types](../data-types/entity-types.md) - Entity type system

## Related Documentation

- [useEntityLookup Hook](../react-query/hooks.md) - Hook that uses registry
- [API Lookup Functions](../../api-integration/api-functions-patterns.md) - Functions registered in registry
