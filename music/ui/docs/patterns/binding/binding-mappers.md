# Binding Response Mappers

## What It Is

Type-safe dispatch pattern for mapping binding API responses to domain entities based on entity type.

## Why It Exists

Provides single interface for binding responses across entity types, enables type-safe mapper selection, and centralizes binding response handling.

## Location

[src/music/data/master/api/music-data-commons.ts](../../src/music/data/master/api/music-data-commons.ts)

## Mapper Registry

**Type:**
- `Record<MasterEntityType, (dto: BindingResponseDTO) => MasterEntity>`

**Structure:**
- `artist`: `createArtistFromBindingResponse`
- `album`: `createAlbumFromBindingResponse`
- `track`: `createTrackFromBindingResponse`
- `category`: `createCategoryFromBindingResponse`

**Name:** `bindingResponseMappers`

## How It Works

### Mapper Selection

**Input:**
- Entity type (e.g., 'artist')
- Binding response DTO

**Process:**
1. Index into mapper registry with entity type
2. Get entity-specific mapper function
3. Call mapper with response DTO
4. Return mapped domain entity

**Output:**
- Domain entity (Artist, Album, etc.)

### Type Safety

**At Compile Time:**
- TypeScript checks entity type is valid key
- Ensures mapper exists for entity type
- Validates mapper signature

**At Runtime:**
- Correct mapper called for entity type
- No type casting needed
- Safe property access

## Usage Pattern

**In API Functions:**
- After binding API call
- Map response DTO to entity
- Use registry for type-safe mapping

**Example:**
- Bind raw artist to master artist
- Response contains master artist DTO
- Use `bindingResponseMappers['artist'](responseDTO)`
- Get Artist domain entity

## Mapper Implementations

### Artist Binding Mapper

Location: [music-data-artists.ts](../../src/music/data/master/api/music-data-artists.ts)

**Input:** Artist binding response DTO

**Output:** Artist domain entity

**Process:**
- Extract artist fields from DTO
- Create ArtistImpl instance
- Return artist

### Album Binding Mapper

Location: [music-data-albums.ts](../../src/music/data/master/api/music-data-albums.ts)

**Input:** Album binding response DTO

**Output:** Album domain entity

**Additional:** May include artist relation

### Track Binding Mapper

Location: [music-data-tracks.ts](../../src/music/data/master/api/music-data-tracks.ts)

**Input:** Track binding response DTO

**Output:** Track domain entity

**Additional:** May include artist relation

### Category Binding Mapper

Location: [music-data-categories.ts](../../src/music/data/master/api/music-data-categories.ts)

**Input:** Category binding response DTO

**Output:** Category domain entity

**Additional:** May include parent relations

## Benefits

**Single Source of Truth:**
- One registry for all binding mappers
- Easy to find and update mappers

**Type Safety:**
- Compile-time checking
- No runtime type errors
- Refactoring safety

**Extensibility:**
- Add new entity type: Add mapper to registry
- Existing code automatically works with new type

## Comparison: Alternative Approaches

### Alternative 1: Switch Statement

**Drawback:** Verbose, error-prone, not type-safe

### Alternative 2: If-Else Chain

**Drawback:** Linear search, harder to maintain

### Mapper Registry (Current)

**Advantages:**
- O(1) lookup
- Type-safe
- Concise
- Maintainable

## Related Patterns

- [DTO Mapping](../../api-integration/api-dto-mapping.md) - General DTO mapping
- [Entity Types](../data-types/entity-types.md) - Entity type system
- [Data Source](../data-fetching/data-source.md) - Similar dispatch pattern

## Related Documentation

- [Binding Workflow](../../flows/binding-workflow.md) - Binding process using mappers
- [API Functions](../../api-integration/api-functions-patterns.md) - Binding API functions
