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

## API Conventions

### Search vs Lookup Endpoints
- **`GET /{entities}/search`** - Full-featured search with pagination, sorting, and detailed results
  - Returns `Page<DetailedDTO>` with hierarchy information
  - Supports complex filtering and sorting
  - Used for management interfaces
  
- **`GET /{entities}/lookup`** - Lightweight search for dropdown lists and autocomplete
  - Returns `List<LookupResultDTO>` with only id and name
  - Default limit of 20 results, configurable via `limit` parameter
  - Used for form dropdowns and quick selection

### Binding
- **`POST /{entities}/bind/existing/{dataSource}/{externalId}`** - Bind external entity to existing entity
- **`POST /{entities}/bind/new/{dataSource}/{externalId}`** - Create new entity from an external entity and bind them
- **`DELETE /{entities}/unbind/{dataSource}/{externalId}`** - Unbind
- **`GET /{entities}/bound/{dataSource}?externalIds=1,2,3`** - Get bound entities for the external entities from a specific data source.

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
# Run from project root directory
./scripts/run-module-dev.sh music-universe:music-data
# Runs on port 7082 with dev profile
```

**Docker Deployment:**
```bash
# Run from project root directory
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
