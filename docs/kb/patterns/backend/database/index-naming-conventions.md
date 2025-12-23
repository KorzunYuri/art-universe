# Index Naming Conventions

## Pattern Overview

Database indexes follow the general pattern: **`{table}_{index_type}[_{related_fields_or_description}]`**

The pattern is consistently used across the database, though specific naming conventions vary by schema and evolution over time.

## Index Type Notation

### Primary Key Indexes

| Convention | Example | Used In |
|------------|---------|---------|
| `{table}_pk` | `album_pk`, `artist_pk`, `track_pk` | mu schema (older tables) |

### Regular Indexes (Foreign Keys / Frequently Queried Columns)

| Convention | Example | Used In |
|------------|---------|---------|
| `{table}_i_{description}` | `album_i_album_group`, `track_i_primary_artist` | mu schema (older tables) |

### Unique Indexes

| Convention | Example | Used In |
|------------|---------|---------|
| `{table}_uk` | `artist_uk`, `category_name_uk` | mu schema - business logic constraints |
| `{table}_ui_{description}` | `album_binding_ui_external_album` | mu schema - technical multi-column constraints |

## Standard Convention

All schemas should follow this convention:

```sql
-- Primary Key
CONSTRAINT album_pk PRIMARY KEY (id)

-- Foreign Key Index
CREATE INDEX album_i_primary_artist ON album(primary_artist_id);

-- Unique Constraint (Business Logic)
CONSTRAINT artist_uk UNIQUE (name)

-- Unique Index (Technical - Multi-column)
CREATE UNIQUE INDEX album_binding_ui_external_album
    ON album_binding(external_id, data_source_id);
```

## Recommendations for New Indexes

When creating new indexes, use the standard convention:

```sql
{table}_pk              -- Primary keys
{table}_i_{description} -- Regular indexes
{table}_uk              -- Unique business constraints
{table}_ui_{description} -- Unique technical constraints
```

## Index Naming Guidelines

1. **Be Descriptive**: Index names should clearly indicate their purpose
   - `album_i_primary_artist` is better than `album_i_pa`

2. **Reference Indexed Columns**: Include column names or their purpose
   - `album_i_primary_artist` indicates indexing on `primary_artist_id`
   - `track_i_track_group` indicates indexing on `track_group_id`

3. **Maintain Consistency**: Always use the standard convention
   - Use `_pk` for primary keys
   - Use `_i_` for regular indexes
   - Use `_uk` for unique business constraints
   - Use `_ui_` for unique technical constraints

4. **Distinguish Index Types**: Use different suffixes for unique vs regular indexes
   - `_uk` / `_ui_` for unique constraints
   - `_i_` for regular indexes

## Legacy Indexes

Some indexes in the database currently use non-standard naming conventions that deviate from the established pattern:

- `_pkey` instead of `_pk` for primary keys
- `idx_{table}_{description}` instead of `{table}_i_{description}` for regular indexes
- `{table}_{column}_unique` instead of `{table}_uk` for unique constraints
- `{table}_{column}_idx` suffix pattern in staging tables

These non-standard indexes are scheduled to be renamed to follow the standard convention in future migration cycles to maintain consistency across the entire database.
