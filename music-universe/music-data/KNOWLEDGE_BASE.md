# Music Universe - Data Service

> **See also**: [Development Guide](../../DEVELOPMENT.md) | [Architecture Overview](../../ARCHITECTURE.md)

## Module Purpose

Central data management service for approved music data. Manages curated entities from external sources and provides binding APIs to link external entities with internal approved entities.

## Key Components

### Entity Management
- `Artist`, `Album`, `Track` - Core approved entities
- `ArtistBinding`, `AlbumBinding`, `TrackBinding` - External entity bindings
- `DataSource` enum - External data source definitions (LASTFM, SPOTIFY, MUSICBRAINZ)

### Service Layer
- `ArtistService` - Artist management and binding operations
- `TrackService` - Track management and binding operations  
- `AlbumService` - Album management and binding operations

### Repository Layer
- Custom `@Query` methods for bulk binding lookups
- Projection interfaces for optimized queries
- Search functionality with pagination and sorting

## REST API Endpoints

### Artist Management
- `GET /api/v1/artists/bound/{dataSource}?externalIds=1,2,3` - Get bound artists
- `GET /api/v1/artists/search?query=name&limit=20` - Search artists by name
- `POST /api/v1/artists/bind/{dataSource}/{externalId}` - Bind external artist
- `DELETE /api/v1/artists/unbind/{dataSource}/{externalId}` - Unbind artist

### Track Management  
- `GET /api/v1/tracks/bound/{dataSource}?externalIds=1,2,3` - Get bound tracks
- `POST /api/v1/tracks/bind/{dataSource}/{externalId}` - Bind external track
- `DELETE /api/v1/tracks/unbind/{dataSource}/{externalId}` - Unbind track

### Album Management
- `GET /api/v1/albums/bound/{dataSource}?externalIds=1,2,3` - Get bound albums

## Business Rules

### Track Binding
1. Artist must be bound before binding tracks
2. Track binding creates internal track if it doesn't exist
3. Track lookup by name and primary artist combination

### Artist Binding
1. Artist binding creates internal artist if it doesn't exist
2. Artist lookup by name only
3. Binding updates allowed (rebinding to different internal entity)

### Search Rules
1. Case insensitive partial matching (LIKE %term%)
2. Default limit of 20 results, configurable
3. Alphabetical sorting by name

## Development

**Local Development:**
```bash
./scripts/run-module-dev.sh music-universe:music-data
# Runs on port 7082 with dev profile
```

**Docker Deployment:**
```bash
./env/docker/deploy.sh local   # Port 9082
./env/docker/deploy.sh prod    # Port 8082
```

## Configuration

### Environment Variables
- `MU_DATA_DB_*` - Database connection parameters
- `MU_DATA_APP_*` - Application server configuration

### Database
- **Schema**: `mu` in PostgreSQL
- **Connection Pool**: HikariCP
- **Migrations**: Liquibase XML changelogs

## Integration Points

- **Music Data Raw Modules** - Provides external entity data for binding
- **Music Quiz Service** - Consumes approved data for quiz generation
- **Music Universe UI** - Management interface for binding operations
- **PostgreSQL** - Shared database with `mu` schema
