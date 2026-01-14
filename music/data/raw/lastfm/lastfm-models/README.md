# LastFM Models

LastFM Models is a shared library module containing all JPA entities, DTOs, and domain models for the LastFM raw data subsystem.


## Entity Structure

### Core Collectables

Core entities representing music data:

- [LastfmArtist](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/LastfmArtist.java) - Music artists with mbid, url, listeners count, play count
- [LastfmAlbum](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/LastfmAlbum.java) - Music albums
- [LastfmTrack](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/LastfmTrack.java) - Music tracks
- [LastfmTag](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/LastfmTag.java) - Genre/category tags

Base classes:
- [BaseLastfmEntity](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/BaseLastfmEntity.java) - Base for all LastFM entities with name field
- [BaseLastfmdomain](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/BaseLastfmCollectable.java) - Base for collectable entities with approvalStatus and apiCall reference

Common fields inherited from base classes:
- `createdAt`, `updatedAt` (audit fields from commons BaseEntity)
- `approvalStatus` (coded enum) - from BaseLastfmCollectable
- `apiCall` (reference to API call that created this entity) - from BaseLastfmCollectable
- `name` - from BaseLastfmEntity

### Entity Relationships

Relationship entities connecting core collectables:

- [LastfmArtistAlbum](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmArtistAlbum.java) - Artist's discography
- [LastfmArtistTrack](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmArtistTrack.java) - Artist's tracks
- [LastfmAlbumTrack](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmAlbumTrack.java) - Album tracklist
- [LastfmArtistsRelation](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmArtistsRelation.java) - Similar artists
- [LastfmArtistTag](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmArtistTag.java) - Artist genres/categories
- [LastfmAlbumTag](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmAlbumTag.java) - Album genres/categories
- [LastfmTrackTag](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/relationship/LastfmTrackTag.java) - Track genres/categories

Base classes:
- [BaseLastfmEntityRelation](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/BaseLastfmEntityRelation.java) - Base for entity relationships
- [BaseLastfmSameEntityRelation](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/BaseLastfmSameEntityRelation.java) - Base for same-entity relationships (artist-artist)

Common fields: foreign keys to related entities, weight or matchValue (relationship strength), audit fields

### API Pipeline Entities

Entities managing the ETL pipeline:

- [LastfmApiCall](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java) - API call task: a planned request to a specific Lastfm API method
- [LastfmApiResponse](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiResponse.java) - Entity containing raw Lastfm API response

Both entities' status flows are managed by [State Machines](../../../../../docs/kb/patterns/backend/state-machine.md)).

### Attribute History

Entities tracking changes to entity attributes over time:

- [LastfmAttributeHistoryRecord](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/attribute/LastfmAttributeHistoryRecord.java) - Historical attribute value (listener counts, play counts)
- [LastfmAttributeSnapshot](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmAttributeSnapshot.java) - Snapshot of all attributes at a point in time
- [LastfmDataSnapshot](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmDataSnapshot.java) - Complete data snapshot

See [SCD2 Attribute History pattern](../../../../../docs/kb/patterns/backend/database/scd2-attribute-history.md)

### Supporting Entities

Additional entities for specific purposes:

- [LastfmArtistSearchRequest](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmArtistSearchRequest.java) - Seed data for artist searches
- [BlacklistedEntityUrl](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/BlacklistedEntityUrl.java) - URLs to exclude from ETL
- [LastfmEntityMetadata](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/LastfmEntityMetadata.java) - Metadata about entity types


## Coded Enums

Enums stored as integers in database using [Coded Enum pattern](../../../../../docs/kb/patterns/backend/entities/coded-enums.md):

- [LastfmEntityType](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/LastfmEntityType.java) - Entity type classification (ARTIST, ALBUM, TRACK, TAG)
- [LastfmEntityRelationType](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/common/LastfmEntityRelationType.java) - Relationship types (SIMILAR, TAGGED_WITH, etc.)
- [LastfmApiCallType](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCallType.java) - API method types (ARTIST_GET_INFO, ALBUM_GET_INFO, etc.)
- [LastfmAttribute](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/attribute/LastfmAttribute.java) - Attribute types for history tracking


## DTOs and Response Models

The module contains DTOs for API response mappings under `api/methods/` organized by API method (artist/getinfo, album/getinfo, etc.)


## Patterns Used

This module implements:

- [State Machine](../../../../../docs/kb/patterns/backend/state-machine.md) - ApiCallStatus and ApiResponseStatus manage pipeline lifecycle
- [Base Entity Pattern](../../../../../docs/kb/patterns/backend/entities/base-entity.md) - Standard entity structure with audit fields
- [Coded Enum Pattern](../../../../../docs/kb/patterns/backend/entities/coded-enums.md) - Type-safe enum storage as integers
- [SCD2 Attribute History](../../../../../docs/kb/patterns/backend/database/scd2-attribute-history.md) - Track attribute changes over time


## Related Documentation

- [LastFM Modules Overview](../README.md) - Overview of all LastFM modules
- [LastFM Repositories](../lastfm-repositories/README.md) - JPA repositories for these entities
- [Backend Entity Patterns](../../../../../docs/kb/patterns/backend/entities/overview.md) - Entity design patterns
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
