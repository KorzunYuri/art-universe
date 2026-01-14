# LastFM Commons Test JPA

The module provides JPA-specific test utilities and base classes for testing LastFM repositories and entities.

## Key Components

### Entity Creation Helper

**Purpose**: Create various entities for test purposes without persisting them.

**Class**: [EntityCreationHelper.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/test/common/entity/EntityCreationHelper.java).

Helper contains two types of methods for creating entities:
- use case specific (e.g. creating an entity with an API call of a specific type as its source)
- signatures with a builder consumer as the only arg, allowing the caller to apply customization on top of default values

**Note that** if the entity is to be saved then it is responsibility of the caller to persist the entities it depends on.
For consistent entities persistence use [lastfm-commons-test-db-helper](../lastfm-commons-test-db-helper/README.md) module.

### Exposed Modules & Dependencies

- [:music:data:raw:lastfm:lastfm-models](../../lastfm-models/README.md) - Lastfm JPA entities
- [:music:data:raw:lastfm:test:lastfm-commons-test](../lastfm-commons-test/README.md) - common Lastfm test utils

## Related Documentation

- [LastFM Modules Overview](../../README.md)
- [LastFM Repositories](../../lastfm-repositories/README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)
