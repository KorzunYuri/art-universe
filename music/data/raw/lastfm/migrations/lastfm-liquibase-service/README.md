# LastFM Liquibase Service

LastFM Liquibase Service is a standalone Java application that executes Liquibase database migrations for the LastFM raw data schema.

It runs as a one-time initialization task or during deployments to apply schema changes from the [lastfm-liquibase-resources module](../lastfm-liquibase-resources/README.md).

The main purpose is to centralize migration process and make the startup easier for modules using the same schema.


## How It Works

### Key Components

- [LiquibaseMigrationRunner](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/migration/liquibase/LiquibaseMigrationRunner.java) - Main class

- [Environment](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/migration/liquibase/Environment.java) - Encapsulates environment variables retrieval logic

### Migration Execution Flow

1. LiquibaseMigrationRunner starts:
   - Extracts environment variables
   - Detects whether it runs in Docker to choose the correct way to load the resources
   - Starts the migration using `liquibase.Liquibase` class
2. Liquibase changelogs are loaded from lastfm-liquibase-resources
3. Connection to PostgreSQL database is established
4. DATABASECHANGELOG table is checked for applied changesets
5. Pending changesets are applied in order
6. DATABASECHANGELOG is updated


## Configuration

### Environment Variables

To run the app correctly, the following variables must be set:

- `MURAW_LASTFM_DB_MASTER_HOST` - PostgreSQL host
- `MURAW_LASTFM_DB_MASTER_PORT` - PostgreSQL port
- `MURAW_LASTFM_DB_NAME` - Database name
- `MURAW_LASTFM_DB_SCHEMA` - Schema name
- `MURAW_LASTFM_DB_WRITER_USERNAME` - Database user
- `MURAW_LASTFM_DB_WRITER_PASSWORD` - Database password
- `MURAW_LASTFM_DB_MIGRATIONS_PATH` - Path to migrations

**Note**: User must have CREATE TABLE, ALTER TABLE, DROP TABLE permissions.


## Build & Deployment

**Build Process**:
1. Copies Liquibase resources from `lastfm-liquibase-resources`
2. Packages JAR with dependencies
3. Creates runnable JAR with manifest


## Related Documentation

- [LastFM Liquibase Resources](../../../../../../docs/kb/modules/lastfm/lastfm-liquibase-resources/README.md) - Changelog files executed by this service
- [LastFM Modules Overview](../../README.md) - Overview of all LastFM modules
- [LastFM Models](../../lastfm-models/README.md) - JPA entities matching the migrated schema
- [Project Modules Index](../../../../../../docs/MODULES.md) - Return to main modules index
