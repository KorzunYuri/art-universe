# Master Data API Reference

This document provides a comprehensive reference for all REST API endpoints in the Music Master Data Management Service.

## Base URL

All endpoints are prefixed with `/api/v1` unless otherwise noted.

## Endpoint Groups

- [Master Entities Management](#master-entities-management)
- [Relationships Management](#relationships-management)
- [Binding Operations](#binding-operations)
- [Read Access Operations](#read-access-operations)
- [Health Check](#health-check)


## Master Entities Management

CRUD operations for core master entities (Artist, Category). Note that Album and Track entities are managed primarily through binding operations.

### Artist Management

**Base Path:** `/api/v1/artists`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/{id}` | Get single artist by ID | Path: `id` | [ArtistDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistDto.java) |
| POST | `/` | Create or update artist | [ArtistSaveRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistSaveRequestDTO.java) | [ArtistDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistDto.java) |
| DELETE | `/{id}` | Delete artist | Path: `id` | `boolean` |
| GET | `/{id}/with-categories` | Get artist with associated categories | Path: `id` | [ArtistWithCategoriesDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistWithCategoriesDto.java) |

### Category Management

**Base Path:** `/api/v1/categories`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/{id}` | Get single category by ID | Path: `id` | [CategoryDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDto.java) |
| POST | `/` | Create or update category | [CategorySaveRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategorySaveRequestDTO.java) | [CategoryDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDto.java) |
| DELETE | `/{id}` | Delete category | Path: `id` | `boolean` |
| GET | `/{id}/with-parents` | Get category with parent relationships | Path: `id` | [CategoryWithParentsDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryWithParentsDto.java) |


## Relationships Management

Endpoints for managing relationships between master entities.

### Artist-Category Relationships

**Base Path:** `/api/v1/artists`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| POST | `/{artistId}/categories/{categoryId}` | Bind artist to category | Path: `artistId`, `categoryId` | `void` |
| DELETE | `/{artistId}/categories/{categoryId}` | Unbind artist from category | Path: `artistId`, `categoryId` | `void` |

### Category Hierarchy

**Base Path:** `/api/v1/categories`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/dag` | Get complete category hierarchy (DAG) | - | [CategoryDagDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDagDTO.java) |
| POST | `/relations` | Create parent-child relationship | [CategoryRelationDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryRelationDTO.java) | `void` |
| DELETE | `/relations` | Delete parent-child relationship | [CategoryRelationDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryRelationDTO.java) | `void` |

### Cross-Entity Relations

**Base Path:** `/api/v1/relations`

These endpoints handle generic relationships between any entity types (Artist-Track, Artist-Album, etc.).

#### Internal Relations

| Method | Path | Purpose | Input                                                          | Response DTO |
|--------|------|---------|----------------------------------------------------------------|--------------|
| GET | `/{sourceEntityType}/{sourceEntityId}/{targetEntityType}` | Get all related entities | Path: `sourceEntityType`, `sourceEntityId`, `targetEntityType` | List<[RelatedEntityDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/RelatedEntityDTO.java)> |
| POST | `/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}` | Create internal relation | Path: all params                                               | `Long` (relation ID) |
| DELETE | `/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}` | Delete internal relation by entities | Path: all params                                               | `boolean` |
| DELETE | `/internal/{relationId}` | Delete internal relation by ID | Path: `relationId`                                             | `boolean` |

Input
## Binding Operations

Endpoints for binding external entities (from LastFM, Spotify, etc.) to master entities and managing those bindings.

### Artist Bindings

**Base Path:** `/api/v1/artists`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/bound/{dataSource}` | Find bound artists by data source | Path: `dataSource`<br>Query: `externalIds` (optional) | List<[BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java)> |
| POST | `/bind/existing/{dataSource}/{externalId}` | Bind external artist to existing master | Path: `dataSource`, `externalId`<br>Body: [EntityBindToExistingRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/EntityBindToExistingRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| POST | `/bind/new/{dataSource}/{externalId}` | Create new master artist and bind external | Path: `dataSource`, `externalId`<br>Body: [EntityCreateAndBindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/EntityCreateAndBindRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| DELETE | `/unbind/{dataSource}/{externalId}` | Unbind external artist | Path: `dataSource`, `externalId` | `boolean` |
| DELETE | `/unbind/{dataSource}/batch` | Batch unbind artists | Path: `dataSource`<br>Body: [BatchUnbindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindRequestDTO.java) | [BatchUnbindResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindResponseDTO.java) |

### Album Bindings

**Base Path:** `/api/v1/albums`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/bound/{dataSource}` | Find bound albums by data source | Path: `dataSource`<br>Query: `externalIds` (optional) | List<[BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java)> |
| POST | `/bind/existing/{dataSource}/{externalId}` | Bind external album to existing master | Path: `dataSource`, `externalId`<br>Body: [ArtistRelatedEntityBindToExistingRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedEntityBindToExistingRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| POST | `/bind/new/{dataSource}/{externalId}` | Create new master album and bind external | Path: `dataSource`, `externalId`<br>Body: [ArtistRelatedEntityCreateAndBindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedEntityCreateAndBindRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| DELETE | `/unbind/{dataSource}/{externalId}` | Unbind external album | Path: `dataSource`, `externalId` | `boolean` |
| DELETE | `/unbind/{dataSource}/batch` | Batch unbind albums | Path: `dataSource`<br>Body: [BatchUnbindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindRequestDTO.java) | [BatchUnbindResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindResponseDTO.java) |

### Track Bindings

**Base Path:** `/api/v1/tracks`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/bound/{dataSource}` | Find bound tracks by data source | Path: `dataSource`<br>Query: `externalIds` (optional) | List<[BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java)> |
| POST | `/bind/existing/{dataSource}/{externalId}` | Bind external track to existing master | Path: `dataSource`, `externalId`<br>Body: [ArtistRelatedEntityBindToExistingRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedEntityBindToExistingRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| POST | `/bind/new/{dataSource}/{externalId}` | Create new master track and bind external | Path: `dataSource`, `externalId`<br>Body: [ArtistRelatedEntityCreateAndBindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedEntityCreateAndBindRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| DELETE | `/unbind/{dataSource}/{externalId}` | Unbind external track | Path: `dataSource`, `externalId` | `boolean` |
| DELETE | `/unbind/{dataSource}/batch` | Batch unbind tracks | Path: `dataSource`<br>Body: [BatchUnbindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindRequestDTO.java) | [BatchUnbindResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindResponseDTO.java) |

### Category Bindings

**Base Path:** `/api/v1/categories`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/bound/{dataSource}` | Find bound categories by data source | Path: `dataSource`<br>Query: `externalIds` (optional) | List<[BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java)> |
| POST | `/bind/existing/{dataSource}/{externalId}` | Bind external category to existing master | Path: `dataSource`, `externalId`<br>Body: [EntityBindToExistingRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/EntityBindToExistingRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| POST | `/bind/new/{dataSource}/{externalId}` | Create new master category and bind external | Path: `dataSource`, `externalId`<br>Body: [EntityCreateAndBindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/EntityCreateAndBindRequestDTO.java) | [BoundEntityProjection](../src/main/java/yurykorzun/art/universe/music/data/master/projection/BoundEntityProjection.java) |
| DELETE | `/unbind/{dataSource}/{externalId}` | Unbind external category | Path: `dataSource`, `externalId` | `boolean` |
| DELETE | `/unbind/{dataSource}/batch` | Batch unbind categories | Path: `dataSource`<br>Body: [BatchUnbindRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindRequestDTO.java) | [BatchUnbindResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchUnbindResponseDTO.java) |

### Relation Bindings

**Base Path:** `/api/v1/relations`

These endpoints handle binding external relationships (e.g., LastFM Artist-Track) to internal master entity relationships.

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}` | Get binding status for relations | Path: `dataSource`, `sourceEntityType`, `sourceExternalEntityId`, `targetEntityType`<br>Query: `ids` (optional) | [RelationBindingStatusDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/RelationBindingStatusDTO.java) |
| POST | `/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}` | Bind external relation to master relation | Path: all params | [RelationBindingDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/RelationBindingDTO.java) |
| DELETE | `/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}` | Unbind external relation | Path: all params | `boolean` |

Input
## Read Access Operations

Endpoints providing read-only access to master entities through search and lookup operations.

### Paginated Search

#### Artists Search

**Base Path:** `/api/v1/artists`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/` | Search artists with pagination | Query: `search` (optional), `categoryId` (optional), `pageable` | Page<[ArtistDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistDto.java)> |
| GET | `/with-categories` | Search artists with categories | Query: `search` (optional), `categoryId` (optional), `pageable` | Page<[ArtistWithCategoriesDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistWithCategoriesDto.java)> |

#### Categories Search

**Base Path:** `/api/v1/categories`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/` | Search categories with pagination | Query: `search` (optional), `pageable` | Page<[CategoryDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDto.java)> |
| GET | `/with-parents` | Search categories with parent relationships | Query: `search` (optional), `pageable` | Page<[CategoryWithParentsDto](../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryWithParentsDto.java)> |

### Lookup Endpoints

Lookup endpoints are optimized for dropdown/autocomplete UI components with limited result sets.

#### Artist Lookup

**Base Path:** `/api/v1/artists`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/lookup` | Lookup artists by search term | Query: `search` (required), `limit` (optional) | List<[LookupResultDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/LookupResultDTO.java)> |
| POST | `/lookup/batch` | Batch lookup artists | [BaseBatchLookupRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BaseBatchLookupRequestDTO.java) | [BatchLookupResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchLookupResponseDTO.java) |

#### Album Lookup

**Base Path:** `/api/v1/albums`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/lookup` | Lookup albums with optional artist filtering | Query: `search` (required), `dataSource` (optional), `masterArtistId` (optional), `externalArtistId` (optional), `limit` (optional) | List<[LookupResultDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/LookupResultDTO.java)> |
| POST | `/lookup/batch` | Batch lookup albums | [ArtistRelatedBatchLookupRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedBatchLookupRequestDTO.java) | [BatchLookupResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchLookupResponseDTO.java) |

#### Track Lookup

**Base Path:** `/api/v1/tracks`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/lookup` | Lookup tracks with optional artist filtering | Query: `search` (required), `dataSource` (optional), `masterArtistId` (optional), `externalArtistId` (optional), `limit` (optional) | List<[LookupResultDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/LookupResultDTO.java)> |
| POST | `/lookup/batch` | Batch lookup tracks | [ArtistRelatedBatchLookupRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedBatchLookupRequestDTO.java) | [BatchLookupResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchLookupResponseDTO.java) |

#### Category Lookup

**Base Path:** `/api/v1/categories`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/lookup` | Lookup categories by search term | Query: `search` (required), `limit` (optional) | List<[LookupResultDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/LookupResultDTO.java)> |
| POST | `/lookup/batch` | Batch lookup categories | [BaseBatchLookupRequestDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BaseBatchLookupRequestDTO.java) | [BatchLookupResponseDTO](../src/main/java/yurykorzun/art/universe/music/data/master/dto/BatchLookupResponseDTO.java) |

Input
## Health Check

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------------|--------------|
| GET | `/health` | Service health check | - | `Map<String, String>` {"status": "UP"} |

Input
## Common Patterns

### Data Source Parameter

The `{dataSource}` path parameter accepts values from the [DataSource](../src/main/java/yurykorzun/art/universe/music/data/master/entity/DataSource.java) enum (e.g., `LASTFM`, `SPOTIFY`).

### Entity Type Parameter

The `{entityType}` path parameters accept values from the [EntityType](../src/main/java/yurykorzun/art/universe/music/data/master/entity/EntityType.java) enum (e.g., `ARTIST`, `ALBUM`, `TRACK`, `CATEGORY`).

### Pagination

Search endpoints support Spring Data pagination parameters:
- `page`: Page number (0-based)
- `size`: Page size
- `sort`: Sort criteria (e.g., `name,asc`)

For more details, see the [REST API Conventions](../../../docs/kb/patterns/backend/api/conventions.md).

### Batch Operations

Batch lookup and unbind operations process multiple items in a single request, returning detailed success/failure information per item.

Input
## See Also

- [Entity Relations Reference](entity-relations.md) - Detailed explanation of entity relationships
- [Features Documentation](features/) - Special features and implementation details
- [REST API Conventions](../../../docs/kb/patterns/backend/api/conventions.md) - Project-wide API standards
