# DTO Mapping

## What It Is

Data Transfer Object (DTO) mapping transforms backend JSON responses to domain entity objects and domain objects to backend JSON requests.

## Why It Exists

Separates backend data representation from frontend domain model, enables type-safe transformations, and allows backend changes without affecting domain logic.

## Mapping Direction

**Two-way transformation:**
- **Response DTOs → Domain Entities:** Backend responses to frontend objects
- **Domain Entities → Request DTOs:** Frontend objects to backend requests

## Response DTO Mapping

### Master Data Mappers

Location: [music-data-commons.ts](../../src/music/data/master/api/music-data-commons.ts)

**Mapper Registry:**
- `artist` → `createArtistFromDto`
- `album` → `createAlbumFromDto`
- `track` → `createTrackFromDto`
- `category` → `createCategoryFromDto`

Type-safe dispatch: `masterEntityFromDtoMappers[entityType]`

### Entity-Specific Mappers

Each entity type has dedicated mapper functions:

**Artist Mappers:**

Location: [music-data-artists.ts](../../src/music/data/master/api/music-data-artists.ts)

- `createArtistFromDto` - Basic artist mapping
- `createArtistWithCategoriesFromDto` - Artist with category relations

**Album Mappers:**

Location: [music-data-albums.ts](../../src/music/data/master/api/music-data-albums.ts)

- `createAlbumFromDto` - Basic album mapping
- `createAlbumWithArtistFromDto` - Album with artist relation

**Track Mappers:**

Location: [music-data-tracks.ts](../../src/music/data/master/api/music-data-tracks.ts)

- `createTrackFromDto` - Basic track mapping
- `createTrackWithArtistFromDto` - Track with artist relation

**Category Mappers:**

Location: [music-data-categories.ts](../../src/music/data/master/api/music-data-categories.ts)

- `createCategoryFromDto` - Basic category mapping
- `createCategoryWithParentsFromDto` - Category with parent hierarchy

### LastFM Mappers

Location: [lastfm-common.ts](../../src/music/data/raw/lastfm/api/lastfm-common.ts)

**Mapper Registry:**
- `artist` → `createLastfmArtistFromDto`
- `album` → `createLastfmAlbumFromDto`
- `track` → `createLastfmTrackFromDto`
- `tag` → `createLastfmTagFromDto`

Type-safe dispatch: `lastfmEntityFromDtoMappers[entityType]`

### Binding Response Mappers

Location: [music-data-commons.ts](../../src/music/data/master/api/music-data-commons.ts)

**Mapper Registry:**
- `artist` → `createArtistFromBindingResponse`
- `album` → `createAlbumFromBindingResponse`
- `track` → `createTrackFromBindingResponse`
- `category` → `createCategoryFromBindingResponse`

Type-safe dispatch: `bindingResponseMappers[entityType]`

## Mapper Implementation Pattern

### Simple Mapping

**Input:** DTO interface matching backend JSON
**Output:** Domain entity instance

**Steps:**
1. Extract fields from DTO
2. Create domain entity instance
3. Return entity

### Complex Mapping (With Relations)

**Input:** DTO with nested relation DTOs
**Output:** Domain entity with relation entities

**Steps:**
1. Extract base fields from DTO
2. Create base entity instance
3. Map relation DTOs to relation entities
4. Attach relations to base entity
5. Return entity with relations

### Paged Response Mapping

**Input:** `Page<DTO>` response
**Output:** `Page<Entity>` with mapped content

**Steps:**
1. Map each DTO in `content` array
2. Preserve pagination metadata (`totalPages`, `totalElements`, etc.)
3. Return mapped page

## Request DTO Mapping

### Create Request

**Input:** Domain entity fields
**Output:** Request DTO

Used when creating new entities via POST requests.

### Update Request

**Input:** Entity ID + updated fields
**Output:** Request DTO

Used when updating entities via PUT/PATCH requests.

### Binding Request

**Bind to Existing:**
- Input: `masterId`
- Output: `{ masterId }`

**Bind to New:**
- Input: Entity fields
- Output: DTO with entity fields

## Type Safety

All mappers are type-safe through:

- TypeScript interfaces for DTOs
- Generic type parameters for entity types
- Discriminated unions for entity type dispatch

## Implementation Files

### Master Data

- [music-data-commons.ts](../../src/music/data/master/api/music-data-commons.ts) - Mapper registries
- [music-data-artists.ts](../../src/music/data/master/api/music-data-artists.ts) - Artist DTOs and mappers
- [music-data-albums.ts](../../src/music/data/master/api/music-data-albums.ts) - Album DTOs and mappers
- [music-data-tracks.ts](../../src/music/data/master/api/music-data-tracks.ts) - Track DTOs and mappers
- [music-data-categories.ts](../../src/music/data/master/api/music-data-categories.ts) - Category DTOs and mappers

### LastFM

- [lastfm-common.ts](../../src/music/data/raw/lastfm/api/lastfm-common.ts) - Mapper registries
- [lastfm-artists.ts](../../src/music/data/raw/lastfm/api/lastfm-artists.ts) - Artist DTOs and mappers
- [lastfm-albums.ts](../../src/music/data/raw/lastfm/api/lastfm-albums.ts) - Album DTOs and mappers
- [lastfm-tracks.ts](../../src/music/data/raw/lastfm/api/lastfm-tracks.ts) - Track DTOs and mappers
- [lastfm-tags.ts](../../src/music/data/raw/lastfm/api/lastfm-tags.ts) - Tag DTOs and mappers

## Related Documentation

- [API Functions](./api-functions-patterns.md) - Functions that use mappers
- [Entity Types](../patterns/data-types/entity-types.md) - Domain entity type system
- [Binding Mappers Pattern](../patterns/binding/binding-mappers.md) - Type-safe mapper dispatch
