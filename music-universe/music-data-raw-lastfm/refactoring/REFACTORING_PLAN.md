## Important Notes

### Deprecated Fields
The following fields have been marked as `@Deprecated` in entity classes and should be ignored during processing:

- **LastfmArtist**: `isStreamable`, `isOnTour`
- **LastfmTrack**: `isStreamable`  
- **LastfmAlbum**: `description`
- **all entities**: rank (specific values like usageCount will be used to rank entities dynamically)

These fields are marked in the processing documentation with **[IGNORE - NOT NEEDED]** and should not be processed or extracted as attributes.

---

# Music Data Raw LastFM - Entity and Relationship Refactoring Plan

## Current State Analysis

The current system uses a unified `LastfmEntityRelation` table to handle all relationships between entities. 
This approach stores relationship attributes in the separate `attribute_history` table, making the system complex and hard to maintain.
The aim of refactoring is to simplify data structure, refactor ApiResponse processing and implement processing for responses of remaining Lastfm API methods.

Resources:
- Response schemas: refactoring/apiclient/responses/schemas.
- Response examples: src/test/resources/apiclient/responses.

## Proposed Refactoring: Dedicated Relationship Entities

### Current Entities

#### 1. LastfmArtist
**Existing fields:**
- `id` (BIGSERIAL)
- `name` (VARCHAR(1024))
- `mbid` (VARCHAR(36))
- `url` (VARCHAR(1024))
- `listenersCount` (INTEGER)
- `playCount` (INTEGER)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)

**Fields to add:**
- None - entity is complete based on our needs

**Fields to ignore and remove in the end:**
- isStreamable
- isOnTour

#### 2. LastfmAlbum
**Existing fields:**
- `id` (BIGSERIAL)
- `name` (VARCHAR(1024))
- `mbid` (VARCHAR(36))
- `url` (VARCHAR(4096))
- `playCount` (INTEGER)
- `listenersCount` (INTEGER)
- `publishTs` (TIMESTAMP)
- `description` (TEXT)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)

**Fields to add:**
- None - entity is complete based on our needs

- **Fields to ignore and remove in the end:**
- description

#### 3. LastfmTrack
**Existing fields:**
- `id` (BIGSERIAL)
- `name` (VARCHAR(2048))
- `mbid` (VARCHAR(36))
- `url` (VARCHAR(8192))
- `duration` (INTEGER)
- `listenersCount` (INTEGER)
- `playCount` (INTEGER)
- `artist` (FK to artist)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)

**Fields to add:**
- None - entity is complete based on our needs

**Fields to ignore and remove in the end:**
- isStreamable

#### 4. LastfmTag
**Existing fields:**
- `id` (BIGSERIAL)
- `name` (VARCHAR(1024))
- `url` (VARCHAR(1024))
- `usageCount` (INTEGER)
- `usageUsersCount` (INTEGER)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)

**Fields to add:**
- None - entity is complete based on our needs

### New Relationship Entities

#### 1. ArtistArtist
**Purpose:** Replace generic entity_relation for artist similarities and relations
**Fields:**
- `id` (BIGSERIAL)
- `sourceArtistId` (BIGINT FK to artist)
- `targetArtistId` (BIGINT FK to artist)
- `matchScore` (DECIMAL(5,4)) - similarity coefficient from artist.getSimilar
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (sourceArtistId, targetArtistId)

#### 2. ArtistTag
**Purpose:** Replace generic entity_relation for artist-tag relationships
**Fields:**
- `id` (BIGSERIAL)
- `artistId` (BIGINT FK to artist)
- `tagId` (BIGINT FK to tag)
- `usageCount` (INTEGER) - from artist.getTopTags count field
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (artistId, tagId)

#### 3. AlbumTag
**Purpose:** Replace generic entity_relation for album-tag relationships
**Fields:**
- `id` (BIGSERIAL)
- `albumId` (BIGINT FK to album)
- `tagId` (BIGINT FK to tag)
- `usageCount` (INTEGER) - from album.getTopTags count field
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (albumId, tagId)

#### 4. TrackTag
**Purpose:** Replace generic entity_relation for track-tag relationships
**Fields:**
- `id` (BIGSERIAL)
- `trackId` (BIGINT FK to track)
- `tagId` (BIGINT FK to tag)
- `usageCount` (INTEGER) - from track.getTopTags count field
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (trackId, tagId)

#### 5. ArtistAlbum
**Purpose:** Replace generic entity_relation for artist-album relationships
**Fields:**
- `id` (BIGSERIAL)
- `artistId` (BIGINT FK to artist)
- `albumId` (BIGINT FK to album)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (artistId, albumId)
**Note:** This is a simple relationship without additional attributes

#### 6. AlbumTrack
**Purpose:** Replace generic entity_relation for album-track relationships
**Fields:**
- `id` (BIGSERIAL)
- `albumId` (BIGINT FK to album)
- `trackId` (BIGINT FK to track)
- `position` (INTEGER) - track position in album from album.getInfo tracks @attr.rank
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (albumId, trackId)

### API Methods Analysis

#### Currently Implemented
1. **tag.getTopTags** ✅ - Entry point for tag discovery
2. **tag.getTopArtists** ✅ - Creates tag-artist relationships
3. **tag.getTopTracks** ✅ - Creates tag-track relationships
4. **artist.getInfo** ✅ - Artist details with stats
5. **artist.getTopTags** ✅ - Creates artist-tag relationships
6. **artist.getTopTracks** ✅ - Creates artist-track relationships
7. **artist.getTopAlbums** ✅ - Creates artist-album relationships
8. **artist.getSimilar** ✅ - Creates artist similarity relationships
9. **artist.search** ✅ - Artist discovery

#### Not Yet Implemented
1. **album.getInfo** ❌ - Album details with tracks and tags
2. **album.getTopTags** ❌ - Creates album-tag relationships
3. **track.getInfo** ❌ - Track details with album and tags
4. **track.getTopTags** ❌ - Creates track-tag relationships
5. **tag.getInfo** ❌ - Tag details with wiki information
6. **tag.getTopAlbums** ❌ - Creates tag-album relationships

### Migration Strategy

### Implementation Priority

1. **High Priority:** ArtistTag, TrackTag (most commonly used)
2. **Medium Priority:** ArtistSimilarity, ArtistAlbum
3. **Low Priority:** AlbumTag, AlbumTrack (requires implementing missing API methods)

### Implementation Tasks

#### Phase 1: Create New Relationship Tables
1. [x] Create new relationship entity classes
2. [x] Create corresponding database tables
3. [x] Add new repositories and services

#### Phase 2: Implement New Logic
1. Implement processors for new relationship entities
2. Update API call generators to use new relationships
3. Implement data consistency checks

#### Phase 3: Data Migration
1. [x] Create migration scripts to populate new tables from existing data
2. [x] Validate data integrity
3. [x] Update queries to use new tables

#### Phase 4: Cleanup
1. [x] Remove references to `LastfmEntityRelation`
2. [x] Drop old tables and related code
3. Simplify attribute handling

**Note:** No dual write implementation needed - new logic will be implemented from scratch for simplicity.

### Benefits of This Approach

1. **Type Safety:** Each relationship has its own strongly-typed entity
2. **Performance:** Direct joins instead of complex attribute queries
3. **Clarity:** Relationship-specific attributes are clearly defined
4. **Maintainability:** Easier to understand and modify relationship logic
5. **Extensibility:** Easy to add new relationship types or attributes

### Implementation Priority

1. **High Priority:** ArtistTag, TrackTag (most commonly used)
2. **Medium Priority:** ArtistSimilarity, ArtistAlbum
3. **Low Priority:** AlbumTag, AlbumTrack (requires implementing missing API methods)

### Database Schema Changes

```sql
-- Example for ArtistTag relationship
CREATE TABLE artist_tag (
    id                  BIGSERIAL PRIMARY KEY,
    artist_id           BIGINT NOT NULL REFERENCES artist(id),
    tag_id              BIGINT NOT NULL REFERENCES tag(id),
    usage_count         INTEGER,
    api_call_id         BIGINT NOT NULL REFERENCES api_call(id),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE(artist_id, tag_id)
);

CREATE INDEX artist_tag_I_artist ON artist_tag(artist_id);
CREATE INDEX artist_tag_I_tag ON artist_tag(tag_id);
```

This refactoring will significantly simplify the codebase while providing better performance and maintainability for relationship management.


## Code style and cleanup

- use **var** where it makes sense.