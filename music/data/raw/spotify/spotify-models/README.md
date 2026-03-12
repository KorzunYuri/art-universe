# Spotify Models

Shared module containing JPA entities and DTOs for the Spotify raw data subsystem. Used by all Spotify ETL modules, the REST API, and repositories.

## Entity Structure

- [domain/entity/](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/domain/entity) - Core Entities
- [etl/entity/](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity) - ETL pipeline entities and coded enums
- [integration/dto/](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/integration/dto) - Java record DTOs for Spotify Web API JSON responses

## Patterns Used

This module follows these project-wide patterns:

- [Coded Enums](../../../../../docs/kb/patterns/backend/entities/coded-enums.md) - All enums use integer codes with `CodedRegistry`
- [Base Entity](../../../../../docs/kb/patterns/backend/entities/base-entity.md) - `SpotifyApiCall` extends `ApiCall` base class

## Related Documentation

- [Spotify Repositories](../spotify-repositories/README.md) - JPA repositories for these entities
- [Spotify ETL Pipeline](../etl/README.md) - ETL pipeline using these models
- [Spotify Modules Overview](../README.md) - All Spotify modules
