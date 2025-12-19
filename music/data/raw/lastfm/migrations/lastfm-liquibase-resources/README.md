# LastFM Liquibase Resources

LastFM Liquibase Resources is a resources-only module containing Liquibase changelog files (XML) that define the LastFM raw data database schema.


## Structure

### Changelog Files

**Location**: `src/main/resources/db/changelog/`

**Master Changelog**: `db-changelog-master.xml` - References all incremental changesets

**Incremental Changesets**: Organized by version or feature:
- `001-initial-schema.xml`
- `002-add-approval-status.xml`
- `003-add-relationship-tables.xml`
- etc.

### Schema Contents

The Liquibase changelogs define:
- Tables for entities defined in [Lastfm Models module](../../lastfm-models/README.md) and used across the Lastfm raw data subsystem
- Attribute History staging tables and functions
- Maintenance functions
- Indexes and foreign keys for created tables


## Liquibase Conventions

### Changeset IDs

- Format: `<version>-<description>` (e.g., `001-initial-schema`)
- Author: Developer name or team identifier
- Each changeset is idempotent

### Naming Conventions

- All table names are lowercase with underscores as words separators
- Indexes, FKs and other constraints follow the pattern `[table]_[constraint type]_[column(s) or purpose of index]`, constraint type being `I|FK|U` for Index, Foreign Key and Unique respectively, e.g.
  - `CREATE INDEX artist_tag_I_artist ON artist_tag (artist_id)`
  - `ALTER TABLE artist_binding ADD CONSTRAINT artist_binding_fk_artist FOREIGN KEY (master_id) REFERENCES artist(id) ON DELETE CASCADE;`
  - `ALTER TABLE track ADD CONSTRAINT track_u_name_artist_id UNIQUE (name, artist_id);`


### Rollback Support

Changesets may include rollback instructions for reversible migrations.


## Version Control

All changelog files are version-controlled in Git. Schema changes follow this workflow:

1. Create new changeset file
2. Add reference to master changelog
3. Test migration locally
4. Commit and push
5. Execute via liquibase-service in deployment


## Related Documentation

- [LastFM Modules Overview](../../README.md) - Overview of all LastFM modules
- [LastFM Liquibase Service](../lastfm-liquibase-service/README.md) - Executes these migrations
- [LastFM Models](../../lastfm-models/README.md) - JPA entities matching this schema
- [Project Modules Index](../../../../../../docs/MODULES.md) - Return to main modules index
