# Music Universe - LastFM Data Collector

> **See also**: [Development Guide](../../DEVELOPMENT.md) | [Architecture Overview](../../ARCHITECTURE.md)

## Module Purpose

Spring Boot application that collects data about tags, artists, albums, and tracks from the LastFM public API for further processing in the Art Universe system.

## Key Components

### API Client Layer
- `LastfmApiClientImpl` - HTTP client for LastFM API with rate limiting
- `LastfmApiCall` / `LastfmApiResponse` - API call tracking entities
- `LastfmApiCallGenerator` - Generates API calls based on entity state

### Data Processing Layer
- `LastfmApiResponseProcessor` - Processes API responses into entities
- `EntityFactory` - Maps DTOs to domain entities
- `EntityAttributeHandler` - Manages attribute history (SCD2)

### Entity Layer
- `LastfmArtist`, `LastfmAlbum`, `LastfmTrack`, `LastfmTag` - Core entities
- `LastfmEntityRelation` - Entity relationships
- `LastfmAttributeHistoryRecord` - Attribute change tracking

### Scheduling Layer
- `LastfmApiCallScheduler` - Coordinates API call generation/execution
- `LastfmApiResponseProcessingScheduler` - Processes responses asynchronously

## API Endpoints

- `GET /api/v1/artists` - Search/filter artists with pagination
- `PATCH /api/v1/artists/{id}/approval` - Update artist approval status
- `GET /api/v1/tracks` - Search/filter tracks with pagination  
- `PATCH /api/v1/tracks/{id}/approval` - Update track approval status
- `GET /api/v1/tags` - Search/filter tags with pagination
- `PATCH /api/v1/tags/{id}/approval` - Update tag approval status

## Database Schema

- **Entities**: `artist`, `album`, `track`, `tag`
- **Relations**: `artist_artist`, `artist_album`, `artist_track`, `artist_tag`, `album_track`, `album_tag`, `track_tag`
- **API Tracking**: `api_call`, `api_response`
- **Attributes**: `attribute_history`, `attribute_snapshot`
- 
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

#### ArtistArtist
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

#### ArtistAlbum
**Fields:**
- `id` (BIGSERIAL)
- `artistId` (BIGINT FK to artist)
- `albumId` (BIGINT FK to album)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (artistId, albumId)

#### ArtistTrack
**Fields:**
- `id` (BIGSERIAL)
- `artistId` (BIGINT FK to artist)
- `trackId` (BIGINT FK to track)
- `approvalStatus` (SMALLINT)
- `apiCall` (FK to api_call)
- `createdAt` (TIMESTAMP)
- `updatedAt` (TIMESTAMP)

**Unique constraint:** (artistId, trackId)

#### ArtistTag
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

#### AlbumTrack
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

#### AlbumTag
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

#### TrackTag
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

## Configuration

### Key Settings
- **API Rate Limiting**: 1.0 calls/sec (configurable)
- **Retry Policy**: 3 attempts with exponential backoff
- **Thresholds**: Configurable minimums for listeners, play counts, usage counts
- **Scheduling**: Configurable intervals for different API call types

### Environment Variables
- `MURAW_LASTFM_API_KEY` - LastFM API key
- `MURAW_LASTFM_DB_*` - Database connection parameters
- `MURAW_LASTFM_APP_*` - Application server configuration

## Development

**Local Development:**
```bash
# Run from project root directory
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm
# Runs on port 7081 with dev profile
```

**Docker Deployment:**
```bash
# Run from project root directory
./env/docker/deploy.sh local   # Port 9081
./env/docker/deploy.sh prod    # Port 8081
```

## Data Flow

1. **API Call Generation** - Based on entity state, business logic per-method and last api call due dates
2. **API Execution** - Rate-limited calls to LastFM API
3. **Response Processing** - Parse JSON responses into DTOs and update entities, relations and attribute history
   1. **Entity Mapping** - Convert DTOs to domain entities
   1. **Relationship Creation** - Link related entities
   1. **Attribute Extraction** - Detect and apply changed attributes (+ SCD2)

## Integration Points

- **Music Data Module** - Consumes approved entities for binding
- **Music Universe UI** - Provides management interface
- **PostgreSQL** - Persistent storage with `mu_raw_lastfm` schema
