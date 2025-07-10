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
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm
# Runs on port 7081 with dev profile
```

**Docker Deployment:**
```bash
./env/docker/deploy.sh local   # Port 9081
./env/docker/deploy.sh prod    # Port 8081
```

## Data Flow

1. **API Call Generation** - Based on entity state, business logic per-method and last api call due dates
2. **API Execution** - Rate-limited calls to LastFM API
3. **Response Processing** - Parse JSON responses into DTOs
4. **Entity Mapping** - Convert DTOs to domain entities
5. **Attribute Extraction** - Detect and apply changed attributes (+ SCD2)
6. **Relationship Creation** - Link related entities

## Integration Points

- **Music Data Module** - Consumes approved entities for binding
- **Music Universe UI** - Provides management interface
- **PostgreSQL** - Persistent storage with `mu_raw_lastfm` schema
