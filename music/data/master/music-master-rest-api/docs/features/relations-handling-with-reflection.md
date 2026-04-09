# External Relation Binding

This document describes the mechanism of handling relations between master entities and binding/unbinding relations of external entities to them in an unified way.

For a detailed explanation of entity relations design see [Entity Relations Reference](../entity-relations.md)

## Key Components

The core components of the feature are:
- [RelationService.java](../../src/main/java/yurykorzun/art/universe/music/data/master/service/RelationService.java) - Defines relation binding operations
- [RelationServiceImpl.java](../../src/main/java/yurykorzun/art/universe/music/data/master/service/RelationServiceImpl.java) - Implements RelationService: orchestrates relation binding, uses native SQL queries via EntityManager

## Key Operations

- createInternalRelation() - Creates an internal master relation without any external binding.
- deleteInternalRelation() - Removes an internal master relation (also cascades to delete any external bindings pointing to it).
- bindExternalRelation() - Creates or updates a binding between an external relation and an internal master relation. Validates that both entities are bound first.
- unbindExternalRelation() - Removes the binding record only (keeps the internal master relation intact).
- findBoundExternalRelations() - Batch query showing which target entities have bound relations with a source entity. Returns detailed status for each target.
- getRelatedEntities() - Retrieves all entities related to a source entity through internal master relations (ignoring external bindings).

## Binding Flow

Example: Binding LastFM Artist 123 → Track 456

1. Validate
   1. Check ArtistBinding: external_id=123 → master_id=1 ✓
   2. Check TrackBinding: external_id=456 → master_id=2 ✓
2. Create metadata. RelationMetadata determines:
   1. Relation table: "artist_track"
   2. Binding table: "artist_track_binding"
   3. Field names for queries
3. Find or create internal relation
   1. Query: artist_track WHERE artist_id=1 AND track_id=2
      1. If found: use relation ID = 100
      2. If not found: INSERT new relation
4. Find or update binding 
   1. Query: artist_track_binding WHERE data_source='LASTFM' AND external_artist_id=123 AND external_track_id=456
      1. If found: UPDATE master_id
      2. If not found: CREATE new binding
5. Return RelationBindingDTO with all IDs and entity names

## Supporting Components

### Master Relation Entities

Internal relationships between master entities.

- [RelationEntity.java](../../src/main/java/yurykorzun/art/universe/music/data/master/relation/RelationEntity.java) - Contract for all master relation entities (provides entity types and IDs)
- [ArtistTrack.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/ArtistTrack.java) - Relation between Artist and Track master entities
- [ArtistCategory.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/ArtistCategory.java) - Relation between Artist and Category master entities
- [CategoryCategory.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/CategoryCategory.java) - Hierarchical relation between Categories

### Relation Binding Entities

Bridge entities that link external relations to internal master relations.

- [RelationBindingEntity.java](../../src/main/java/yurykorzun/art/universe/music/data/master/relation/RelationBindingEntity.java) - Contract for all relation binding entities (provides external IDs, data source, master binding ID)
- [ArtistTrackBinding.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/ArtistTrackBinding.java) - Binds external Artist-Track to master ArtistTrack (Key Fields: `master_id` (FK), `data_source_id`, `external_artist_id`, `external_track_id`)
- [ArtistCategoryBinding.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/ArtistCategoryBinding.java) - Binds external Artist-Category to master ArtistCategory (Key Fields: Same structure)

### Metadata & Registry Classes

Critical classes that enable dynamic handling of different relation types.

- [RelationMetadata.java](../../src/main/java/yurykorzun/art/universe/music/data/master/relation/RelationMetadata.java) - Central to the design - knows relation table names, binding table names, field names, entity ordering for each relation type
- [RelationRegistry.java](../../src/main/java/yurykorzun/art/universe/music/data/master/relation/RelationRegistry.java) - Maps entity type pairs to their corresponding relation and binding entity classes
- [RelationKey.java](../../src/main/java/yurykorzun/art/universe/music/data/master/relation/RelationKey.java) - Identifies relation types by entity type pair (FirstEntityType, SecondEntityType)
- [MasterEntityMetadata.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/MasterEntityMetadata.java) - Provides table and field names for entity types

### Response DTOs

- [RelationBindingDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/relation/RelationBindingDTO.java) - Complete binding information after bind operation
- [RelationBindingStatusDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/relation/RelationBindingStatusDTO.java) - Batch query showing binding status of source entity and multiple targets
- [TargetEntityBindingDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/relation/TargetEntityBindingDTO.java) - Details of target entity binding (flags for entity/relation/binding bound status)
- [RelatedEntityDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/relation/RelatedEntityDTO.java) - Simple DTO for related entities (ID, name, type)

### Enums

- [MasterEntityType.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/MasterEntityType.java) - Entity type identification (ARTIST, ALBUM, TRACK, CATEGORY, DIMENSION)
- [DataSource.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/DataSource.java) - External data source identification (LASTFM, SPOTIFY, MUSICBRAINZ)


## Design Patterns

- Registry Pattern: RelationRegistry maintains dynamic mappings between entity type pairs and classes
- Metadata Pattern: RelationMetadata encapsulates schema knowledge, enabling reflection-free queries
- Two-Layer Architecture: Clear separation between entity binding and relation binding
- Interface Contracts: RelationEntity and RelationBindingEntity provide type safety without reflection


## See Also

- [Master Data API Reference](../api.md#binding-operations) - Relation binding endpoints
- [Relations Handling Using Reflection](relations-handling-with-reflection.md) - Generic relation handling patterns
- [Entity Relations Reference](../entity-relations.md) - Overview of entity relationships
