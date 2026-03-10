# Spotify Repositories

This module provides base Spring Data JPA repository interfaces for the entities defined in [Spotify Models](../spotify-models/README.md).

All repositories are marked with `@NoRepositoryBean`: they are **not** included in Spring Context.

## Why Base Repositories?

**Problem**: Multiple modules need to access the same entities but with different query needs.

**Solution**: Define base repositories in this shared module, then extend them in consuming modules.

**Benefits**:
- Shared method definitions inherited from `JpaRepository`
- Module-specific customization
- No code duplication
- Consistent naming across modules

## Repository Structure

### Core Entity Repositories

- [domain/repository/](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/domain/repository) - Core Entity Repositories
  - **Naming pattern**: `BaseSpotify[Entity]Repository`
- [etl/repository/](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/repository) - API Pipeline Repositories
  - `BaseSpotifyApiCallRepository` - API call tasks
  - `BaseSpotifyApiResponseRepository` - Raw JSON API responses
  - `BaseSpotifyStagingIterationRepository` - Staging iteration lifecycle
  - `BaseSpotifySearchAttemptRepository` - Search attempt tracking and match scoring

## Related Documentation

- [Spotify Models](../spotify-models/README.md) - Entity definitions for these repositories
- [Spotify Modules Overview](../README.md) - Overview of all Spotify modules
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
