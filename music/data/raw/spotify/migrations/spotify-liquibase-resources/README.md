# Spotify Liquibase Resources

Spotify Liquibase Resources is a resources-only module containing Liquibase changelog files (XML) that define the Spotify raw data database schema (`mu_raw_spotify`).

## Structure

### Changelog Files

**Location**: `src/main/resources/db/migration/muraw_spotify/liquibase/`

**Master Changelog**: `db.changelog.xml` - References all incremental changelog files

## Liquibase Conventions

### Changeset IDs

- Format: `<version>_<sequence>_<description>` (e.g., `0001_0010_api_call$initial`)
- Author: `system`

### Naming Conventions

- All table names are lowercase with underscores
- Indexes follow the pattern `idx_[table]_[column(s)]`

## Related Documentation

- [Spotify Modules Overview](../../README.md) - Overview of all Spotify modules
- [Spotify Liquibase Service](../spotify-liquibase-service/README.md) - Executes these migrations
- [Spotify Models](../../spotify-models/README.md) - JPA entities matching this schema
- [Project Modules Index](../../../../../../docs/MODULES.md) - Return to main modules index
