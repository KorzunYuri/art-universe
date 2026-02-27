# API Functions

## What It Is

API functions provide typed interfaces to backend endpoints, following the principle of one function per endpoint.

## Why It Exists

Creates clear boundaries between UI layer and API calls, enables type-safe API communication, and facilitates testing through function mocking.

## Core Principle

**One function per endpoint** - Each backend endpoint has exactly one corresponding TypeScript function.

## Function Organization

### By Domain Module

Functions organized in domain-specific API directories:

- **Master Data:** [src/music/data/master/api/](../../src/music/data/master/api/)
- **LastFM:** [src/music/data/raw/lastfm/api/](../../src/music/data/raw/lastfm/api/)
- **Quiz:** [src/music/quiz/api/](../../src/music/quiz/api/)

### By Operation Type

Within each domain, functions grouped by operation:

**Common Operations:**
- **Fetching:** [music-data-common-fetching.ts](../../src/music/data/master/api/music-data-common-fetching.ts)
- **Lookup:** [music-data-common-lookup.ts](../../src/music/data/master/api/music-data-common-lookup.ts)
- **Binding:** [music-data-common-binding.ts](../../src/music/data/master/api/music-data-common-binding.ts)

**Entity-Specific:**
- **Artists:** [music-data-artists.ts](../../src/music/data/master/api/music-data-artists.ts)
- **Albums:** [music-data-albums.ts](../../src/music/data/master/api/music-data-albums.ts)
- **Tracks:** [music-data-tracks.ts](../../src/music/data/master/api/music-data-tracks.ts)
- **Categories:** [music-data-categories.ts](../../src/music/data/master/api/music-data-categories.ts)

## Endpoint Mapping

### Master Data Pattern

Location: [music-data-commons.ts](../../src/music/data/master/api/music-data-commons.ts)

**Endpoint Map:**
- `artist` → `/artists`
- `album` → `/albums`
- `track` → `/tracks`
- `category` → `/categories`

The endpoint map enables generic functions that work across entity types.

### LastFM Pattern

Location: [lastfm-common.ts](../../src/music/data/raw/lastfm/api/lastfm-common.ts)

**Endpoint Map:**
- `artist` → `/artists`
- `album` → `/albums`
- `track` → `/tracks`
- `tag` → `/tags`

## Function Patterns

### GET Request (Single Entity)

**Purpose:** Fetch individual entity by ID

**Pattern:**
- Function name: `fetch{Module}{Entity}`
- Parameters: `entityType`, `id`
- Returns: `Promise<{Entity}>`
- Maps response DTO to domain entity

**Example:** [fetchMasterEntity](../../src/music/data/master/api/music-data-common-fetching.ts)

### GET Request (Paged Collection)

**Purpose:** Fetch paginated list of entities

**Pattern:**
- Function name: `fetch{Module}{Entities}`
- Parameters: `entityType`, `searchParams` (page, size, sort, search)
- Returns: `Promise<Page<{Entity}>>`
- Maps each DTO in page content to domain entity

**Example:** [fetchMasterEntities](../../src/music/data/master/api/music-data-common-fetching.ts)

### POST Request (Create)

**Purpose:** Create new entity

**Pattern:**
- Function name: `create{Entity}`
- Parameters: entity fields
- Returns: `Promise<{Entity}>`
- Maps request to DTO, response DTO to entity

**Example:** [createArtist](../../src/music/data/master/api/music-data-artists.ts)

### PUT/PATCH Request (Update)

**Purpose:** Update existing entity

**Pattern:**
- Function name: `update{Entity}` or `save{Entity}`
- Parameters: `id`, update fields
- Returns: `Promise<void>` or `Promise<{Entity}>`

**Example:** [saveArtist](../../src/music/data/master/api/music-data-artists.ts)

### DELETE Request

**Purpose:** Delete entity

**Pattern:**
- Function name: `delete{Entity}`
- Parameters: `id`
- Returns: `Promise<void>`

**Example:** [deleteArtist](../../src/music/data/master/api/music-data-artists.ts)

### POST Request (Lookup)

**Purpose:** Search entities by name

**Pattern:**
- Function name: `lookup{Module}{Entities}`
- Parameters: `entityType`, `searchRequest` (search string, limit)
- Returns: `Promise<{Entity}[]>`
- Used for autocomplete and picker components

**Example:** [lookupMasterEntities](../../src/music/data/master/api/music-data-common-lookup.ts)

### POST Request (Batch Lookup)

**Purpose:** Multiple lookups in single request

**Pattern:**
- Function name: `batchLookup{Module}EntitiesWithParams`
- Parameters: `entityType`, array of search requests
- Returns: `Promise<BatchLookupResponseDTO>`
- Optimizes multiple autocomplete queries

**Example:** [batchLookupMasterEntitiesWithParams](../../src/music/data/master/api/music-data-common-lookup.ts)

### POST Request (Binding)

**Purpose:** Bind raw entity to master entity

**Patterns:**

**Bind to Existing:**
- Function name: `bind{Entity}ToExistingMaster`
- Parameters: `dataSource`, `externalId`, `masterId`
- Returns: `Promise<{Entity}>`

**Bind to New:**
- Function name: `bind{Entity}ToNewMaster`
- Parameters: `dataSource`, `externalId`, entity fields
- Returns: `Promise<{Entity}>`

**Example:** [bindRawEntityToExistingMaster](../../src/music/data/master/api/music-data-common-binding.ts)

## Generic vs Specific Functions

### Generic Functions

Work across multiple entity types using type parameters.

**Locations:**
- [music-data-common-fetching.ts](../../src/music/data/master/api/music-data-common-fetching.ts)
- [music-data-common-lookup.ts](../../src/music/data/master/api/music-data-common-lookup.ts)
- [music-data-common-binding.ts](../../src/music/data/master/api/music-data-common-binding.ts)

### Entity-Specific Functions

Handle entity-specific operations (e.g., category parent management, artist-category binding).

**Locations:**
- [music-data-artists.ts](../../src/music/data/master/api/music-data-artists.ts)
- [music-data-categories.ts](../../src/music/data/master/api/music-data-categories.ts)
- [music-data-relations.ts](../../src/music/data/master/api/music-data-relations.ts)

## Array Parameter Handling

For array parameters, values are joined with comma separator:

**Example:** `externalIds.join(',')` for multiple IDs

## Related Documentation

- [DTO Mapping](./api-dto-mapping) - Request/response transformation
- [React Query Hooks](../react-query/hooks.md) - Hook patterns that consume these functions
