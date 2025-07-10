# Music Data Raw LastFM - Entity and Relationship Refactoring Plan

## Current State Analysis

### API Methods

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


#### Resources:
- Response schemas: refactoring/apiclient/responses/schemas.
- Response examples: src/test/resources/apiclient/responses.

## Proposed Refactoring: Dedicated Relationship Entities

### Entities

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

### Relationship Entities

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

## Important Notes

### Deprecated Fields
The following fields have been marked as `@Deprecated` in entity classes and should be ignored during processing:

- **LastfmArtist**: `isStreamable`, `isOnTour`
- **LastfmTrack**: `isStreamable`
- **LastfmAlbum**: `description`
- **all entities**: rank (specific values like usageCount will be used to rank entities dynamically)

These fields are marked in the processing documentation with **[IGNORE - NOT NEEDED]** and should not be processed or extracted as attributes.

### Code style and cleanup

- use **var** where it makes sense.