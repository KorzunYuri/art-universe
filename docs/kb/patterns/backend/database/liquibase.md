# Liquibase Migration Pattern

## Purpose

Standard approach for managing database schema changes using Liquibase in Spring Boot modules.


## What Belongs in Migrations vs. Init Script

The `env/docker/common/db/initdb/01-init.sh` applies numbered SQL changesets from
`env/docker/common/db/initdb/changesets/`. The split between init script and Liquibase is:

**Init script only** — things Liquibase literally cannot do:
- `CREATE DATABASE` — must run before the target DB exists
- `CREATE USER` — passwords come from env vars; storing them in version-controlled migrations is a security anti-pattern
- `CREATE SCHEMA … AUTHORIZATION` — schema ownership must be set before the module's Liquibase runner connects
- `ALTER SCHEMA … OWNER TO` — ownership transfers requiring superuser context
- `GRANT CREATE ON DATABASE` — superuser privilege
- `GRANT USAGE ON SCHEMA mu_view TO PUBLIC` — must be set before any module starts, since all modules depend on it at boot time
- Replication slot + pg_hba.conf — server-level operations

**Liquibase migrations** — everything else:
- `ALTER DEFAULT PRIVILEGES` — who can access future objects in each schema
- `GRANT … ON SCHEMA` for non-bootstrap access (e.g. reader users, cross-schema grants)
- `GRANT … ON TABLE/SEQUENCE` — fine-grained access tied to specific feature migrations
- All DDL (tables, views, indexes, functions)

**Consequence**: adding a new user or changing access policy for an existing DB is a single Liquibase migration — no manual SQL required.


## Bootstrap Grants — The `0000-schema-grants` Pattern

Every Liquibase module has a `0000-schema-grants` changelog that is **listed first** in `db.changelog.xml`. It runs as the module's dm user (schema owner) and establishes:

1. `ALTER DEFAULT PRIVILEGES IN SCHEMA … GRANT ALL ON TABLES/SEQUENCES/FUNCTIONS TO <dm-user>` — ensures the dm user can manage all objects it creates going forward
2. Read grants for any companion read-only user (e.g. `mu_raw_lastfm_reader`, `mu_raw_spotify_reader`)
3. **Cross-schema grants where this module is the grantor** — e.g. the `art` module grants `mu_dm` access to `art_view`; the `mu` module grants `mu_quiz_dm` access to `mu_view`

The principle for cross-schema grants: **the grant lives in the module that owns the schema being accessed**, not the module that needs it. This is because only the schema owner can issue the grant.

These migrations are safe to run on existing databases — `GRANT`, `ALTER DEFAULT PRIVILEGES`, and `GRANT USAGE` are all idempotent in PostgreSQL.

### Example

```sql
-- music/data/master: 0000-schema-grants
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON TABLES TO mu_dm;

-- Cross-schema: mu_quiz_dm reads mu_view (grantor = mu_dm, owner of mu_view)
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO mu_quiz_dm;
```

## Migration File Location

The root for all migrations is `src/main/resources/db/migration/{module-specific-name}/liquibase/`

- db.changelog.xml - Master Changelog
- changelogs/{4-digit-order-number}-{changelog-description}.xml - Changelog files
- changesets/{4-digit-order-number}-{changelog-description}/{4-digit changeset order number}-{changeset-description}.xml - Changeset files

### Master Changelog (db.changelog.xml)

The `db.changelog.xml` file references all individual changelog files:

```xml
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.15.xsd">

    <include file="changelogs/0001-artist.xml" relativeToChangelogFile="true"/>
    <include file="changelogs/0002-album.xml" relativeToChangelogFile="true"/>
    <include file="changelogs/0003-dictionary.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

### Changelog Files (changelogs/*.xml)

Each changelog XML file defines changesets that reference SQL files:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.15.xsd">

    <changeSet id="0001-0010-artist$initial" author="developer">
        <sqlFile path="../changesets/0001-artist/0001-0010-artist$initial.sql"
                 relativeToChangelogFile="true"/>
    </changeSet>

    <changeSet id="0001-0020-artist_binding$initial" author="developer">
        <sqlFile path="../changesets/0001-artist/0001-0020-artist_binding$initial.sql"
                 relativeToChangelogFile="true"/>
    </changeSet>

</databaseChangeLog>
```

### SQL Changeset Files (changesets/*/*.sql)

Actual SQL migration scripts organized by topic in subdirectories


## Running Migrations

### Automatic Execution

Migrations run **automatically on application startup** by default.

### Configuration

Liquibase is configured in `application.yml`:

```yaml
spring:
  liquibase:
    enabled: true    # Enable/disable migrations
    change-log: classpath:db/migration/mu/liquibase/db.changelog.xml
```

### Controlling Execution

**Disable migrations** (for testing or special scenarios):

```yaml
spring:
  liquibase:
    enabled: false
```

## Migration File Naming

### Naming Convention

Use sequential numbers with descriptive names:

**Changelog files** (in `changelogs/`):
```
0001-artist.xml
0002-album.xml
0003-dictionary.xml
0004-track.xml
```

**SQL changeset files** (in `changesets/{topic}/`):
```
0001-0010-artist$initial.sql
0001-0020-artist_binding$initial.sql
0002-0010-album$initial.sql
0003-0010-dictionary$initial.sql
```

**Pattern**:
- Changelogs: `{number}-{description}.xml`
- Changesets: `{changelog-number}-{changeset-number}-{description}.sql`

### Guidelines

- **Sequential numbers** - Use 0001, 0002, 0003, etc. for changelogs
- **Sub-numbers** - Use 0010, 0020, 0030, etc. for changesets within a changelog
- **Descriptive names** - Clearly describe the change
- **Lowercase with hyphens** - kebab-case naming
- **XML format** - Use `.xml` for changelog files
- **SQL format** - Use `.sql` for actual migration scripts
- **Topic folders** - Group related changesets in subdirectories


## Migration Best Practices

### 1. Never Modify Existing Migration Files

**Once a migration is committed and deployed, never modify it.**

```
✅ Good: Create new changelog and changeset files
❌ Bad: Edit existing migration files
```

**Reason**: Liquibase tracks executed migrations by checksums. Modifying an existing file will cause errors.

### 2. Always Create New Migration Files

For any schema change, create **new files**:

1. Add a new changelog XML file in `changelogs/`
2. Create a new topic folder in `changesets/`
3. Add SQL migration files in the topic folder
4. Reference the new changelog in `db.changelog.xml`

**Example**: Adding a category column to artist

```xml
<!-- changelogs/0005-add-artist-category.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.15.xsd">

    <changeSet id="0005-0010-add-category-column" author="developer">
        <sqlFile path="../changesets/0005-add-artist-category/0005-0010-add-category-column.sql"
                 relativeToChangelogFile="true"/>
    </changeSet>

</databaseChangeLog>
```

```sql
-- changesets/0005-add-artist-category/0005-0010-add-category-column.sql
ALTER TABLE artist ADD COLUMN category VARCHAR(50);
```

### 3. Use Meaningful Names

Use sequence numbers with clear descriptions:

```
✅ Good: 0003-dictionary.xml
✅ Good: 0007-category-binding.xml
❌ Bad: migration.xml
❌ Bad: 0003-changes.xml
```

### 4. Organize Changesets by Topic

Group related SQL migration files in topic-based folders:

```
changesets/
├── 0001-artist/           # All artist-related migrations
├── 0002-album/            # All album-related migrations
├── 0003-dictionary/       # All dictionary-related migrations
└── 0004-track/            # All track-related migrations
```

**Note**: Rollback strategies should be documented but implemented as forward migrations.


## Seed Data Conventions

### Hardcoded IDs for System Data

System seed data (reference data that must be consistent across all environments)
uses hardcoded IDs in a reserved range above auto-generated sequence values.

**ID scheme** (example from relation types):

| Entity | ID pattern | Example |
|---|---|---|
| `relation_type` | `1000 + ordinal` | 1001 = "Is Primary Artist Of", 1002 = "Contains" |
| `relation_type_applicability` | `{parent_id} * 1000 + ordinal` | 1001001, 1001002, 1002001 |

Auto-generated IDs from application sequences start well below 1000, so there is
no collision risk.

### Idempotent Seed SQL

Seed changesets use `ON CONFLICT DO NOTHING` to be safely re-runnable:

```sql
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1001, 'Is Primary Artist Of', 'Has Primary Artist', FALSE, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT 1001001, rt.id, 1, 2, TRUE
FROM relation_type rt
WHERE rt.name = 'Is Primary Artist Of'
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;
```

**Key points**:
- Each INSERT uses a unique hardcoded ID — avoid multi-row INSERTs with a single ID
- `ON CONFLICT` targets the appropriate unique constraint for each table
- Use `SELECT ... FROM` to reference parent rows by name rather than hardcoding FKs

**Example**: [System relation types seed](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/changesets/0016-relation-types/0016-0130-system_relation_types$seed.sql)


## Liquibase Tables

Liquibase creates two tables to track migrations:

- **`databasechangelog`** - Records executed changesets
- **`databasechangeloglock`** - Manages concurrent execution

**Do not manually modify these tables.**


## Examples in Codebase

- [Music Quiz](../../../../../music/quiz/src/main/resources/db/migration/muquiz/liquibase/)
- [Music Data Master](../../../../../music/data/master/src/main/resources/db/migration/mu/liquibase/)
- [Music Data Lastfm](../../../../../music/data/raw/lastfm/migrations/lastfm-liquibase-resources/src/main/resources/db/migration/muraw/liquibase)


## See Also

- [Index Naming Conventions](index-naming-conventions.md) - Index naming conventions
- [Entity Patterns Overview](../entities/overview.md) - Entity design patterns
- [Project Structure Pattern](../project-structure.md) - Module structure
- [SCD2 Attribute History](./scd2-attribute-history.md) - Historical data tracking
