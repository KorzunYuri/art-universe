# Music Universe - Master Data Management Service

## Module Purpose

Central data management service for music master data. Manages master entities from external sources and provides binding APIs to link external entities with internal master entities.

> **See also**: [Entity Relations Docs](./ENTITY_RELATIONS.md)

> **See also**: [Development Guide](../../DEVELOPMENT.md) | [Architecture Overview](../../ARCHITECTURE.md)

## Key Components

### Entity Management
- `Artist`, `Album`, `Track` - Core master entities
- `Category`, `Dimension` - Classification entities
- `ArtistBinding`, `AlbumBinding`, `TrackBinding`, `CategoryBinding` - External entity bindings
- `DataSource` enum - External data source definitions (LASTFM, SPOTIFY, MUSICBRAINZ)

### Relation Management
- `ArtistCategory`, `ArtistTrack` - Internal relations between master entities
- `ArtistCategoryBinding`, `ArtistTrackBinding` - External relation bindings
- `RelationEntity`, `RelationBindingEntity` - Interfaces for relation entities

### Service Layer
- `{Entity}Service` - Entity management and binding operations
- `RelationService` - Entity relation management operations

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

### Entity Binding
- **`POST /{entities}/bind/existing/{dataSource}/{externalId}`** - Bind external entity to existing master entity
- **`POST /{entities}/bind/new/{dataSource}/{externalId}`** - Create new master entity from an external entity and bind them
- **`DELETE /{entities}/unbind/{dataSource}/{externalId}`** - Unbind
- **`GET /{entities}/bound/{dataSource}?externalIds=1,2,3`** - Get bound master entities for the external entities from a specific data source.

### Relation Management
- **`POST /relations/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}`** - Bind external relation to internal relation
- **`DELETE /relations/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}`** - Unbind external relation
- **`GET /relations/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}?ids=[targetExternalEntityIds]`** - Get bound relations
- **`GET /relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}`** - Get related entities
- **`POST /relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}`** - Create internal relation
- **`DELETE /relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}`** - Delete internal relation by entity types and IDs
- **`DELETE /relations/internal/{relationId}`** - Delete internal relation by relation ID

## Business Rules

### Track Binding
1. Artist must be bound before binding tracks
2. Track binding creates internal master track if it doesn't exist
3. Track lookup by name and primary artist combination

### Artist Binding
1. Artist binding creates internal master artist if it doesn't exist
2. Artist lookup by name only
3. Binding updates allowed (rebinding to different master entity)

### Category Binding
1. Categories can be organized in hierarchies with parent-child relationships
2. Categories belong to dimensions (e.g., genre, mood, era)
3. Category lookup supports hierarchical display

### Relation Binding
1. Both entities must be bound before binding their relation
2. External relation binding requires or creates an internal relation
3. Unbinding an external relation doesn't delete the internal relation

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
- **Music Quiz Service** - Consumes master data for quiz generation
- **Music Universe UI** - Management interface for binding operations
- **PostgreSQL** - Shared database with `mu` schema
