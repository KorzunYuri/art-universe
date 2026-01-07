# LastFM REST API

The LastFM REST API module provides read-only HTTP access to raw LastFM data collected by the ETL pipeline.

It serves as the primary data access layer for querying LastFM entities (artists, albums, tracks, tags) with search and lookup operations.


## Key Components

### Controllers

**Collectable Controllers** (`collectable/controller/`):
- [LastfmArtistController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmArtistController.java) - Artist search and lookup
- [LastfmAlbumController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmAlbumController.java) - Album search and lookup
- [LastfmTrackController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmTrackController.java) - Track search and lookup
- [LastfmTagController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmTagController.java) - Tag search and entity-tag relationships

**General Controllers**:
- [HealthController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/controller/HealthController.java) - Health check endpoint

### Services

**Collectable Services** (`collectable/service/`):
- [LastfmArtistService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmArtistService.java) - Artist retrieval and searching
- [LastfmAlbumService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmAlbumService.java) - Album retrieval and searching
- [LastfmTrackService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmTrackService.java) - Track retrieval and searching
- [LastfmTagService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmTagService.java) - Tag retrieval and searching

**Lookup Services**:
- [LastfmEntityLookupService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmEntityLookupService.java) - Generic entity lookup by code
- [LastfmArtistLookupService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmArtistLookupService.java) - Artist-specific lookup
- [LastfmTagLookupService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmTagLookupService.java) - Tag-specific lookup

### Repositories

Module extends JPA repositories defined in [lastfm-repositories](lastfm-repositories/README.md) modules.

### DTOs

Data transfer objects for API requests and responses (`collectable/dto/`):
- Search parameters: `ArtistSearchParams`, `AlbumSearchParams`, `TrackSearchParams`, `TagSearchParams`
- Entity relationships: `EntityTagDto`, `EntityTagSearchParams`

### Common Components

Configuration and error handling (`common/`):
- [TransactionConfig.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/config/TransactionConfig.java) - Read-only transaction configuration
- [WebConfig.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/config/WebConfig.java) - CORS and web MVC configuration
- [GlobalExceptionHandler.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/exception/GlobalExceptionHandler.java) - Centralized exception handling
- [MaintenanceException.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/exception/MaintenanceException.java) - Thrown when ETL maintenance is in progress


## API Endpoints

| Method | Path                              | Purpose                          |
|--------|-----------------------------------|----------------------------------|
| GET    | `/api/lastfm/artists/search`      | Search artists                   |
| GET    | `/api/lastfm/artists/{code}`      | Lookup artist by code            |
| GET    | `/api/lastfm/artists/{code}/tags` | Get tags for artist              |
| GET    | `/api/lastfm/albums/search`       | Search albums                    |
| GET    | `/api/lastfm/albums/{code}`       | Lookup album by code             |
| GET    | `/api/lastfm/tracks/search`       | Search tracks                    |
| GET    | `/api/lastfm/tracks/{code}`       | Lookup track by code             |
| GET    | `/api/lastfm/tags/search`         | Search tags                      |
| GET    | `/api/lastfm/tags/{code}`         | Lookup tag by code               |
| GET    | `/api/lastfm/tags/{code}/artists` | Get artists for tag              |
| GET    | `/api/lastfm/tags/{code}/albums`  | Get albums for tag               |
| GET    | `/api/lastfm/tags/{code}/tracks`  | Get tracks for tag               |
| GET    | `/api/health`                     | Health check endpoint            |


## Configuration

### Environment Variables

- `MURAW_LASTFM_DB_HOST` - PostgreSQL database host
- `MURAW_LASTFM_DB_PORT` - PostgreSQL database port
- `MURAW_LASTFM_DB_NAME` - Database name
- `MURAW_LASTFM_DB_SCHEMA` - Database schema
- `MURAW_LASTFM_DB_USER_NAME` - Database username
- `MURAW_LASTFM_DB_PASSWORD` - Database password
- `MURAW_LASTFM_REST_API_INTERNAL_PORT` - Application HTTP port
- `MURAW_LASTFM_REST_API_CORS_ALLOWED_ORIGINS` - CORS allowed origins

### Deployment Notes

The module currently shares the same database instance as [ETL pipeline](etl/README.md) and [ETL REST API](etl/lastfm-etl-rest-api/README.md).

**Planned**: In production, reads will be redirected to a read replica with physical replication. This introduces potential synchronization lag between write operations (via ETL REST API) and read operations (via this module), which must be handled in the UI layer.


## Build & Deployment

**See**: [Gradle Commands Guide](../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../env/docker/README.md) for deployment procedures


## Testing

The module includes comprehensive test coverage:

- **Controller Tests**: Unit tests and MVC tests following [Controller Testing Pattern](../../../../docs/kb/patterns/backend/testing/testing-controllers.md)
- **Service Tests**: Business logic tests for search and lookup operations
- **Repository Tests**: JPA repository tests
- **DTO Tests**: Parameter validation tests
- **Exception Handling Tests**: Integration tests for error scenarios


## Patterns Used

This module follows these project-wide patterns:

- [Controller Testing Pattern](../../../../docs/kb/patterns/backend/testing/testing-controllers.md) - Unit tests and MVC tests for all controllers
- [API Conventions](../../../../docs/kb/patterns/backend/api/conventions.md) - RESTful API design and DTO usage
- [Base Entity](../../../../docs/kb/patterns/backend/entities/base-entity.md) - All entities extend base classes


## Related Documentation

- [LastFM Modules Overview](README.md) - Overview of all LastFM modules
- [LastFM ETL Pipeline](etl/README.md) - ETL pipeline that populates the data
- [Project Modules Index](../../../../docs/MODULES.md) - Return to main modules index
