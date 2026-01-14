# LastFM REST API

The LastFM REST API module provides read-only HTTP access to raw LastFM data collected by the ETL pipeline.

It serves as the primary data access layer for querying LastFM entities (artists, albums, tracks, tags) with search and lookup operations.

It is configured to read data from replica.

## Key Components

### Controllers

**Domain Controllers** (`domain/controller/`):
- [LastfmArtistController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/controller/LastfmArtistController.java) - Artist search and lookup
- [LastfmAlbumController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/controller/LastfmAlbumController.java) - Album search and lookup
- [LastfmTrackController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/controller/LastfmTrackController.java) - Track search and lookup
- [LastfmTagController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/controller/LastfmTagController.java) - Tag search and entity-tag relationships

**General Controllers**:
- [HealthController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/controller/HealthController.java) - Health check endpoint

### Services

**Domain Services** (`domain/service/`):
- [LastfmArtistService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/LastfmArtistService.java) - Artist retrieval and searching
- [LastfmAlbumService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/LastfmAlbumService.java) - Album retrieval and searching
- [LastfmTrackService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/LastfmTrackService.java) - Track retrieval and searching
- [LastfmTagService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/LastfmTagService.java) - Tag retrieval and searching

**Lookup Services**:
- [LastfmEntityLookupService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/lookup/LastfmEntityLookupService.java) - Generic entity lookup by code
- [LastfmArtistLookupService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/lookup/LastfmArtistLookupService.java) - Artist-specific lookup
- [LastfmTagLookupService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/lookup/LastfmTagLookupService.java) - Tag-specific lookup

### Repositories

Module extends JPA repositories defined in [lastfm-repositories](../lastfm-repositories/README.md) modules.

### DTOs

Data transfer objects for API requests and responses (`domain/dto/`):
- Search parameters: `ArtistSearchParams`, `AlbumSearchParams`, `TrackSearchParams`, `TagSearchParams`
- Entity relationships: `EntityTagDto`, `EntityTagSearchParams`

### Common Components

Configuration and error handling (`common/`):
- [TransactionConfig.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/TransactionConfig.java) - Read-only transaction configuration
- [WebConfig.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/WebConfig.java) - CORS and web MVC configuration

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

- `MURAW_LASTFM_DB_REPLICA_HOST` - PostgreSQL replica host
- `MURAW_LASTFM_DB_REPLICA_PORT` - PostgreSQL replica port
- `MURAW_LASTFM_DB_NAME` - Database name
- `MURAW_LASTFM_DB_SCHEMA` - Database schema
- `MURAW_LASTFM_DB_READER_USERNAME` - Lastfm data reader username
- `MURAW_LASTFM_DB_READER_PASSWORD` - Lastfm data reader password
- `MURAW_LASTFM_REST_API_INTERNAL_PORT` - Application HTTP port
- `MURAW_LASTFM_REST_API_CORS_ALLOWED_ORIGINS` - CORS allowed origins
- `ZIPKIN_BASE_URL` - Zipkin URL

### Development Environment Setup

When running in dev mode (IntelliJ), environment variables are loaded in this order:
1. `env/docker/common/music-data-raw-lastfm.env` - Lastfm constants
2. `env/docker/dev/common.env` - Dev common settings
3. `env/docker/dev/music-data-raw-lastfm.env` - Dev env variables
4. `env/docker/dev/music-data-raw-lastfm.secrets.env` - Dev secrets (Git-ignored)

**Prerequisites**:
- Dev stack must be running: `docker-compose -f env/docker/dev/docker-compose.yml up -d`
- See [DEVELOPMENT.md](../../../../../docs/DEVELOPMENT.md) for complete dev workflow

### Deployment Notes

The module reads from a PostgreSQL read replica configured via streaming replication.
Write operations are performed by the [ETL pipeline](../etl/README.md) and [ETL REST API](../etl/lastfm-etl-rest-api/README.md) on the master database.

**Replication Lag Handling**: There is potential synchronization lag between write operations (master) and read operations (replica).
The UI layer handles this through optimistic updates - changes are reflected immediately in the UI cache without waiting for replication, ensuring users see their modifications instantly.


## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../env/docker/README.md) for deployment procedures


## Testing

The module includes comprehensive test coverage:

- **Controller Tests**: Unit tests and MVC tests following [Controller Testing Pattern](../../../../../docs/kb/patterns/backend/testing/testing-controllers.md)
- **Service Tests**: Business logic tests for search and lookup operations
- **Repository Tests**: JPA repository tests
- **DTO Tests**: Parameter validation tests
- **Exception Handling Tests**: Integration tests for error scenarios


## Patterns Used

This module follows these project-wide patterns:

- [Controller Testing Pattern](../../../../../docs/kb/patterns/backend/testing/testing-controllers.md) - Unit tests and MVC tests for all controllers
- [API Conventions](../../../../../docs/kb/patterns/backend/api/conventions.md) - RESTful API design and DTO usage
- [Base Entity](../../../../../docs/kb/patterns/backend/entities/base-entity.md) - All entities extend base classes


## Related Documentation

- [LastFM Modules Overview](README.md) - Overview of all LastFM modules
- [LastFM ETL Pipeline](../etl/README.md) - ETL pipeline that populates the data
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
