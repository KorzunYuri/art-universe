# SCD2 Attribute History Tracking

## Purpose

Tracks historical changes to entity attributes from external APIs, preserving complete history for time-series analysis, trend detection, and data quality auditing. This pattern enables querying attribute values at any point in time without losing historical data.

## Originates from

Initial implementation in the LastFM raw data collection module for tracking time-varying attributes like listener counts, play counts, and rankings.

## Known usages

Used in modules:
- [LastFM Raw Data](../../../modules/mu-data-raw-lastfm/README.md) - tracks all entity attributes (artist listeners, track play counts, etc.)

## When to Use

Use this pattern when:
- Tracking time-varying attributes from external APIs (listeners, play counts, rankings)
- Need to query historical values at any point in time
- Analyzing trends and changes over time
- Auditing data quality and detecting anomalies in external data
- Building time-series datasets for analytics or machine learning

Do NOT use when:
- Tracking immutable attributes that never change (entity IDs, fixed classifications)
- Storage space is critically constrained and history is not needed
- Real-time queries on current values only (consider materialized views for current state)

## Key Concept

SCD2 (Slowly Changing Dimension Type 2) maintains a complete history of attribute values by adding temporal validity columns (`valid_from`, `valid_till`) to each record. Current records have `valid_till = '9999-12-31'`, while historical records have `valid_till` set to the day before the change occurred.

**Why Art Universe does it this way**: External API data changes frequently (daily for some attributes), and we need to:
1. Track changes for trend analysis and quiz difficulty calibration
2. Audit data quality issues from upstream sources
3. Support future analytics features without re-collecting historical data
4. Maintain referential integrity with specific API calls that collected the data

---

## Implementation Steps

### Step 1: Create the Base Table with SCD2 Columns

Create a table with the standard SCD2 temporal columns plus your domain-specific attributes.

**Example**: [Lastfm Migration](../../../../../music/data/raw/lastfm/migrations/lastfm-liquibase-resources/src/main/resources/db/migration/muraw/liquibase/changesets/0003-entity-tables/0003_0030_attribute_history$initial.sql)

**Key Points**:
- `valid_from` and `valid_till` are DATE columns (not TIMESTAMP) for efficient range queries
- Default `valid_till = '9999-12-31'` represents "current" or "no end date"
- `collection_ts` preserves exact API call time for auditing
- Scope columns allow attributes that vary by context (e.g., artist popularity in different countries)

**Art Universe Implementation**: See migration files below for complete schema

### Step 2: Add Unique Constraints for Data Integrity

Ensure no duplicate records exist for the same entity/attribute/validity period.

**Example**: [Lastfm Migration](../../../../../music/data/raw/lastfm/migrations/lastfm-liquibase-resources/src/main/resources/db/migration/muraw/liquibase/changesets/0006-attribute-snapshots/0006-0050_attribute_history$add_value_uniquity_constraint.sql)

**Key Points**:
- Use `COALESCE` to include NULL values in unique constraints
- This prevents inserting duplicate records for the same date
- Constraint on `valid_from` (not `valid_till`) because that's when the new value becomes active

### Step 3: Create Staging Tables for Batch Processing

Use staging tables with double-buffering to avoid locking the main table during batch inserts.

**Example**: [Lastfm Migration](../../../../../music/data/raw/lastfm/migrations/lastfm-liquibase-resources/src/main/resources/db/migration/muraw/liquibase/changesets/0034-attribute-history-staging/0034-0010-create-staging-tables.sql)

**Key Points**:
- Two staging tables allow one to be written to while the other is being processed
- Unique constraint enables `ON CONFLICT ... DO UPDATE` for deduplication
- Staging tables are truncated after each processing cycle

### Step 4: Implement Staging Upsert with Deduplication

Write incoming attribute values to the staging table, automatically deduplicating to keep only the latest value.

**Example**: [Lastfm Service](../../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/attribute/LastfmAttributeHistoryServiceImpl.java)

**Key Points**:
- `ON CONFLICT ... DO UPDATE` ensures only the latest value is kept in staging
- Reduces staging table size and processing time
- `COALESCE` in conflict clause matches the unique constraint definition

### Step 5: Implement SCD2 Merge Logic

Process staging records by expiring changed values and inserting new ones in a single transaction.

**Example**: [LastfmAttributeHistoryProcessor](../../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/attribute/LastfmAttributeHistoryProcessor.java)

**Key Points**:
- `IS DISTINCT FROM` handles NULL comparisons correctly (NULL != NULL is false, but NULL IS DISTINCT FROM NULL is false)
- `valid_till = valid_from - 1 day` ensures no gaps in date ranges
- Only process records where values actually changed (skips unchanged values)
- Entire operation in one transaction ensures consistency

### Step 6: Create JPA Entity

Map the table to a JPA entity with proper defaults for SCD2 columns.

**Example**: [LastfmAttributeHistoryRecord](../../../../../music/data/raw/lastfm/lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/entity/attribute/LastfmAttributeHistoryRecord.java)

**Key Points**:
- Use `LocalDate` (not `Instant`) for SCD2 date columns
- Set default `validTill = "9999-12-31"` for new records
- Set default `validFrom = LocalDate.now()` for new records
- Make `validTill` settable but `validFrom` immutable after creation

### Step 7: Implement Scheduled Processing with Table Switching

Process staging records periodically, switching between staging tables for continuous operation.

**Example**: [LastfmAttributeHistoryProcessor](../../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/attribute/LastfmAttributeHistoryProcessor.java)

**Key Points**:
- Double-buffering allows continuous writes while processing
- `volatile` ensures thread-safe visibility of current table
- Truncate staging table after successful processing
- Log metrics for monitoring (expired vs inserted counts)

## Best Practices

**Art Universe Standards**:
- Always use `'9999-12-31'` as the "current" date marker (not NULL or far-future timestamps)
- Set `valid_till = valid_from - 1 day` when expiring records to avoid gaps
- Use DATE columns (not TIMESTAMP) for `valid_from`/`valid_till` for efficient range queries
- Use `IS DISTINCT FROM` for null-safe value comparisons in merge logic
- Always deduplicate in staging tables before merging to main table
- Query current values with `WHERE valid_till = '9999-12-31'`, not `WHERE valid_till > CURRENT_DATE`
- Include `collection_ts` to preserve exact API call time for auditing
- Use staging tables for batch processing to minimize locking on main table
- Log metrics (expired/inserted counts) for monitoring data change patterns

**Common Mistakes to Avoid**:
- Setting `valid_till = valid_from` when expiring (creates 1-day gaps between records)
- Using `!=` instead of `IS DISTINCT FROM` for NULL comparisons (misses NULL changes)
- Forgetting `COALESCE` in unique constraints with nullable columns (constraint violations)
- Processing unchanged values (wastes storage and processing time)
- Using TIMESTAMP instead of DATE for validity columns (complicates range queries)
- Querying with `valid_till IS NULL` instead of `valid_till = '9999-12-31'` (index not used)

## Testing

Watch existing test classes for examples:
- [LastfmAttributeHistoryProcessorTest](../../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/test/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/attribute/LastfmAttributeHistoryProcessorTest.java)

- **Key test scenarios**:
1. Inserting new records (first-time attributes)
2. Expiring and inserting when values change
3. Skipping processing for unchanged values
4. Deduplication within staging tables
5. Table switching for double-buffering
6. NULL value handling in scope columns
7. Querying historical values at specific dates


## Related Patterns

This pattern is often used with:
- [Coded Enums](../entities/coded-enums.md) - Attribute types stored as coded enums for type safety
- [Liquibase Migrations](liquibase.md) - Database schema evolution for SCD2 tables

This pattern is an alternative to:
- **Append-Only Log**: SCD2 provides structured temporal queries; append-only is simpler but harder to query
- **Current + Archive Tables**: SCD2 unifies current and historical data in one table
- **Snapshot Tables**: SCD2 is more storage-efficient for slowly changing data


## See Also

- [Liquibase Pattern](liquibase.md) - How to structure migrations for SCD2 tables
- [Testing Database Patterns](../testing/jpa-tests.md) - Testing strategies for temporal data

