# Music Universe - Data Service Knowledge Base

## Project Overview

This Spring Boot application serves as the central data management service for the Art Universe project, specifically within the Music Universe domain. It manages "approved" music data that has been curated from various external sources and provides APIs for binding external entities to internal approved entities.

## Architecture

### Core Components

1. **REST API Layer** - RESTful endpoints for managing music entities
2. **Service Layer** - Business logic for entity management and binding operations
3. **Repository Layer** - Data access using Spring Data JPA with custom queries
4. **Entity Layer** - JPA entities and coded enums for data sources

## Key Concepts

### Entity Types
- **Artists** (`Artist`) - Musical artists/performers
- **Albums** (`Album`) - Music albums/releases  
- **Tracks** (`Track`) - Individual songs/tracks

### Binding System
Each entity type has a corresponding binding entity that links external entities to internal approved entities:
- **ArtistBinding** - Links external artists to internal artists
- **AlbumBinding** - Links external albums to internal albums
- **TrackBinding** - Links external tracks to internal tracks

### Data Sources
External data sources defined in `DataSource` enum:
- **LASTFM** (1) - Last.fm music database
- **SPOTIFY** (2) - Spotify music platform  
- **MUSICBRAINZ** (3) - MusicBrainz open music encyclopedia

## REST API Endpoints

### Artist Management
- `GET /api/v1/artists/bound/{dataSource}?externalIds=1,2,3` - Get bound artists
- `GET /api/v1/artists/search?query=search&limit=20` - Search artists by name (limit is optional)
- `POST /api/v1/artists/bind/{dataSource}/{externalId}` - Bind external artist
- `DELETE /api/v1/artists/unbind/{dataSource}/{externalId}` - Unbind artist

### Track Management  
- `GET /api/v1/tracks/bound/{dataSource}?externalIds=1,2,3` - Get bound tracks
- `POST /api/v1/tracks/bind/{dataSource}/{externalId}` - Bind external track
- `DELETE /api/v1/tracks/unbind/{dataSource}/{externalId}` - Unbind track

### Album Management
- `GET /api/v1/albums/bound/{dataSource}?externalIds=1,2,3` - Get bound albums

### Health Check
- `GET /health` - Application health status

## Service Layer

### ArtistService
- `findBoundArtists(DataSource, List<Long>)` - Find multiple bound artists
- `findArtist(DataSource, Long)` - Find single bound artist  
- `bindArtist(DataSource, Long, ArtistBindingRequestDTO)` - Bind external artist
- `unbindArtist(DataSource, Long)` - Unbind external artist
- `searchArtistsByName(String)` - Search artists by name (case insensitive)
- `searchArtistsByName(String, Integer)` - Search artists by name with limit

### TrackService
- `findBoundTracks(DataSource, List<Long>)` - Find multiple bound tracks
- `findTrack(DataSource, Long)` - Find single bound track
- `bindTrack(DataSource, Long, TrackBindingRequestDTO)` - Bind external track
- `unbindTrack(DataSource, Long)` - Unbind external track

### AlbumService
- `findBoundAlbums(DataSource, List<Long>)` - Find multiple bound albums

## Database Schema

### Core Entities
- **Artist**: `id`, `name`, audit timestamps
- **Album**: `id`, `name`, `primary_artist_id`, `album_group_id`, audit timestamps
- **Track**: `id`, `name`, `primary_artist_id`, `track_group_id`, audit timestamps

### Binding Entities
- **ArtistBinding**: `id`, `reference_id`, `data_source_id`, `external_id`, audit timestamps
- **AlbumBinding**: `id`, `reference_id`, `data_source_id`, `external_id`, audit timestamps  
- **TrackBinding**: `id`, `reference_id`, `data_source_id`, `external_id`, audit timestamps

### Dictionary Table
- Stores coded enum values: `domain`, `code`, `name`

## Repository Layer

### Custom Query Methods
- `ArtistBindingRepository.findBoundArtistsForDataSource()` - Bulk artist lookup
- `ArtistBindingRepository.findBoundArtistForDataSource()` - Single artist lookup
- `ArtistRepository.findByNameContainingIgnoreCase(String, int)` - Search artists by name with sorting and limit
- `TrackBindingRepository.findBoundTracksForDataSource()` - Bulk track lookup
- `ArtistRepository.findByName()` - Find artist by name
- `TrackRepository.findByNameAndPrimaryArtistId()` - Find track by name and artist

## Configuration

### Database
- **Database**: PostgreSQL with `mu` schema
- **Connection Pool**: HikariCP (max 10 connections)
- **Migrations**: Liquibase

### Application Properties
- **Port**: 8080 (configurable via `MU_APP_PORT`)
- **Database**: Configurable via `MU_DB_HOST`, `MU_DB_PORT`, `MU_DB_PASSWORD_DM`

### CORS
- Allows requests from `http://localhost:5173` (React development server)

## Design Patterns

### Repository Pattern
- Spring Data JPA repositories with custom `@Query` methods
- Projection interfaces for optimized queries

### Service Pattern  
- Service interfaces with implementation classes
- Transactional operations and business rule validation

### DTO Pattern
- Request DTOs for API input validation
- Projection interfaces for query results
- Response wrappers for consistent API responses

### Coded Enum Pattern
- Enums implement `Coded` interface
- Automatic registration in `CodedRegistry`
- Database synchronization via `CodedRegistrySynchronizer`

## Testing Architecture

### Test Archetypes
- **JpaOnlyTest** - Persistence layer testing with TestContainers
- **FullContextTest** - Integration testing with full Spring Boot context

### Testing Tools
- **JUnit 5** - Test framework
- **Mockito** - Mocking framework (`@MockitoBean`)
- **TestContainers** - Database testing
- **Spring Boot Test** - Integration testing

## Business Rules

### Track Binding Rules
1. Artist must be bound before binding tracks
2. Track binding creates internal track if it doesn't exist
3. Track lookup is by name and primary artist combination

### Artist Binding Rules  
1. Artist binding creates internal artist if it doesn't exist
2. Artist lookup is by name only
3. Binding updates are allowed (rebinding to different internal entity)

### Search Rules
1. Artist search is case insensitive
2. Artist search uses partial matching (LIKE %term%)
3. Empty or null search terms return empty results
4. Default limit of 20 results can be overridden with limit parameter
5. Results are sorted alphabetically by name in ascending order

### Data Consistency Rules
1. All entities extend `BaseEntity` with audit timestamps
2. Foreign key constraints ensure referential integrity
3. Unique constraints prevent duplicate bindings

## Error Handling

### Exception Types
- `IllegalStateException` - Business rule violations (e.g., artist not bound)
- `EntityNotFoundException` - Entity not found after expected creation

### API Error Responses
```json
{
  "success": false,
  "message": "Error description", 
  "data": null
}
```

## Development Workflow

### Local Development
1. Start PostgreSQL database
2. Run Liquibase migrations  
3. Start Spring Boot application
4. Access REST APIs at `http://localhost:8080`

### Testing
```bash
# Run all tests
./gradlew :music-universe:music-data:test

# Run only unit tests (exclude integration)
./gradlew :music-universe:music-data:test -PexcludeIntegrationTests

# Run only integration tests  
./gradlew :music-universe:music-data:integrationTest
```

### Database Migrations
- Located in `src/main/resources/db/migration/mu/liquibase/`
- Organized by entity type (artist, album, track, dictionary)
- Automatic execution on application startup

## Integration Points

### External Systems
- **Music Data Raw Services** - Provides external entity data
- **Music Universe UI** - Consumes REST APIs for data management
- **Music Quiz Service** - Will consume approved data for quiz generation

### Internal Dependencies
- **Art Universe Commons** - Shared utilities and base classes
- **PostgreSQL Database** - Data persistence
- **Liquibase** - Database schema management

## Future Enhancements

### Planned Features
1. Album binding functionality (bind/unbind operations)
2. Tag management and category binding
3. Advanced search and filtering capabilities
4. Bulk operations for binding management
5. Audit logging for binding operations

### Technical Improvements
1. Caching layer for frequently accessed data
2. Event-driven architecture with messaging
3. API versioning strategy
4. Enhanced error handling and validation
5. Performance monitoring and metrics
6. API documentation with OpenAPI/Swagger
