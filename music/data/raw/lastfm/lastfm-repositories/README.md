# LastFM Repositories

The module provides basic Spring Data JPA repository interfaces for the entities defined in [LastFM Models Module](../../../../../docs/kb/modules/lastfm/lastfm-models/README.md) used by LastFM raw data subsystem.

All repositories are marked with @NoRepositoryBean: they are **not** included in Spring Context.

## Why Base Repositories?

**Problem**: Multiple modules need to access the same entities but with different query needs.
Special case is testing, when we usually need only the basic OOTB methods to create specific relationships between entities in a test DB.

**Solution**: Define base repositories in this shared module, then extend them in consuming modules.

**Benefits**:
- Shared method definitions:
  - methods inherited from `org.springframework.data.jpa.repository.JpaRepository`
  - methods defined in current module (currently none)
- Module-specific customization
- No code duplication
- Consistent naming across modules


## Repository Structure

### Collectable Entity Repositories

**Location**: [collectable/repository/](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/repository)

**Naming pattern**: `BaseLastfm[Entity]Repository`, entity being Artist|Album|Track|Tag

### Relationship Repositories

**Location**: [collectable/repository/relationship/](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/repository/relationship)

**Naming patterns**: 
- for different entities: `BaseLastfm[ScopeEntity][ChildEntity]Repository`. 
  - the combinations follow strict 'parent-(grand)child' pattern, so ArtistTag relation is possible, while TagArtist is not
- for same entitiies: `BaseLastfm[Entity]RelationRepository`
  - currently only Artist to Artist relation is supported (addressing `artist.getSimilar` Lastfm API method which links artists by similarity coeff)

### API Pipeline Repositories

**Location**: [api/client/repository/](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/repository)
**Repositories**: 
- `BaseLastfmApiCallRepository` - for API call tasks
- `BaseLastfmApiResponseRepository` - for API responses containing raw JSON data

### Attribute History Repositories

**Location**: [collectable/repository/attribute/](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/repository/attribute)

- `BaseLastfmDataSnapshotRepository` - "Root" data snapshots
- `BaseLastfmAttributeSnapshotRepository` - Attribute-related sub-snapshots
- `BaseLastfmAttributeHistoryRecordRepository` - Attribute changes tracking

### Supporting Repositories

- `BaseLastfmArtistSearchRequestRepository` - History of artist searches
- `BaseBlacklistedEntityUrlRepository` - Entities blacklist (by URL)

## Related Documentation

- [LastFM Models](../../../../../docs/kb/modules/lastfm/lastfm-models/README.md) - Entity definitions for these repositories
- [LastFM Modules Overview](../README.md) - Overview of all LastFM modules
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
