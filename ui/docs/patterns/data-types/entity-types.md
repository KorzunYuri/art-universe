# Entity Type System

## What It Is

Type-safe entity hierarchy with base interfaces, master/raw entity distinction, and type discriminators for runtime type checking.

## Why It Exists

Provides type safety for entities from different sources, enables generic entity handling while preserving type information, and supports entity binding workflows.

## Location

[src/music/shared/types/entities.ts](../../src/music/shared/types/entities.ts)

## Entity Hierarchy

### BaseEntity

**Purpose:** Minimal entity interface

**Fields:**
- `id`: number
- `name`: string

### MasterEntity<T>

**Purpose:** Canonical master entity

**Extends:** `BaseEntity`

**Methods:**
- `getEntityType()`: Returns entity type
- `getMasterEntity()`: Returns self (for compatibility with RawEntity interface)

### RawEntity<T>

**Purpose:** External source entity

**Extends:** `BaseEntity`

**Fields:**
- `masterEntity`: Optional master entity binding

**Methods:**
- `getDataSource()`: Returns data source (e.g., 'lastfm')
- `getEntityType()`: Returns entity type
- `getMasterEntity()`: Returns bound master entity or undefined
- `setMasterEntity()`: Sets/updates master entity binding

### ArtistRelatedRawEntity<T>

**Purpose:** Raw entity related to artist (album, track)

**Extends:** `RawEntity<T>`

**Methods:**
- `getExternalArtistId()`: Returns raw artist ID
- `getMasterArtistId()`: Returns master artist ID if bound

## Entity Type Discriminators

**Type Parameter:** `T extends MasterEntityType`

**Valid Values:**
- `'artist'`
- `'album'`
- `'track'`
- `'category'`

## Master Entity Implementations

### ArtistImpl

Location: Implementation in master data types

**Additional Fields:**
- `categories`: Optional array of categories

### AlbumImpl

**Additional Fields:**
- `primaryArtistId`: Number (master artist ID)

### TrackImpl

**Additional Fields:**
- `primaryArtistId`: Number (master artist ID)

### CategoryImpl

**Additional Fields:**
- `parents`: Optional array of parent categories

## Raw Entity Implementations (LastFM)

### LastfmArtist

Location: [lastfm-artist.ts](../../src/music/data/raw/lastfm/types/lastfm-artist.ts)

**Additional Fields:**
- `url`: Last.fm URL
- `mbid`: MusicBrainz ID
- `playCount`: Play count
- `listenersCount`: Listener count
- `approvalStatus`: Approval status enum

### LastfmAlbum

**Additional Fields:**
- Artist relation (external + master artist IDs)
- LastFM-specific metadata

**Implements:** `ArtistRelatedRawEntity<'album'>`

### LastfmTrack

**Additional Fields:**
- Artist relation (external + master artist IDs)
- LastFM-specific metadata

**Implements:** `ArtistRelatedRawEntity<'track'>`

### LastfmTag

**Simple entity:** No artist relation, minimal fields

## Type Guards

### isArtistRelatedEntity

Location: [entities.ts](../../src/music/shared/types/entities.ts)

**Purpose:** Runtime type check for artist-related entities

**Logic:**
- Check entity type is 'track' or 'album'
- Check presence of `getExternalArtistId` method
- Check presence of `getMasterArtistId` method

**Returns:** Type predicate `entity is ArtistRelatedRawEntity<T>`

**Usage:** Enables type-safe context creation and method calls

## Type Maps

### MasterEntityMap

**Purpose:** Map entity type to master entity class

**Structure:**
- `artist`: Artist
- `album`: Album
- `track`: Track
- `category`: Category

### RawEntityMap (per data source)

**Purpose:** Map entity type to raw entity class

**Structure (LastFM):**
- `artist`: LastfmArtist
- `album`: LastfmAlbum
- `track`: LastfmTrack
- `tag`: LastfmTag (maps to Category)

## Type-Safe Entity Handling

### Generic Functions

Use type parameters to maintain type safety:
- `function processEntity<T extends MasterEntityType>(type: T, entity: MasterEntityMap[T])`

### Discriminated Unions

Use entity type as discriminator:
- `type AnyEntity = { type: 'artist', data: Artist } | { type: 'album', data: Album }`

## Binding Relationship

**Raw Entity → Master Entity:**
- Raw entity has optional `masterEntity` field
- Field populated after binding
- Enables navigation from raw to master

**Master Entity ← Raw Entities:**
- Master entity doesn't track raw entities
- Bindings managed by backend
- Query bindings separately if needed

## Related Patterns

- [Data Source](../data-fetching/data-source.md) - Data source abstraction
- [Type Guards](./type-guards.md) - Runtime type checking
- [Binding Mappers](../binding/binding-mappers.md) - Type-safe mapper dispatch

## Related Documentation

- [DTO Mapping](../../api-integration/api-dto-mapping.md) - DTO to entity mapping
- [Binding Workflow](../../flows/binding-workflow.md) - Entity binding process
