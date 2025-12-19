# Database Patterns Overview

This document provides a quick reference guide to all database-related patterns used in the Art Universe project.


## Available Patterns

### [Liquibase Migrations](liquibase.md)

**Problem**: Need structured, version-controlled database schema changes that can be applied consistently across environments.

**Solution**: Use Liquibase with organized changeset files following Art Universe's naming conventions and directory structure.

### [SCD2 Attribute History Tracking](scd2-attribute-history.md)

**Problem**: Need to track historical changes to entity attributes from external APIs while maintaining the ability to query values at any point in time.

**Solution**: Use SCD2 (Slowly Changing Dimension Type 2) with `valid_from` and `valid_till` date columns. Current records have `valid_till = '9999-12-31'`, historical records have `valid_till` set to the day before change occurred.

**Used In**: [LastFM Raw Data Module](../../../modules/mu-data-raw-lastfm/README.md)


## Common Database Design Principles

All database patterns in Art Universe follow these core principles:

### 1. Referential Integrity
- Use foreign key constraints to maintain data consistency
- Define cascade rules explicitly (CASCADE, RESTRICT, SET NULL)
- Document relationships in entity diagrams

### 2. Indexing Strategy
- Create indexes on foreign keys used in joins
- Use partial indexes for filtered queries (e.g., `WHERE valid_till = '9999-12-31'`)
- Consider composite indexes for multi-column queries
- Monitor index usage and remove unused indexes

### 3. Naming Conventions
- Tables: lowercase with underscores (e.g., `attribute_history`)
- Columns: lowercase with underscores (e.g., `valid_from`)
- Constraints: `{table}${type}_{columns}` (e.g., `attribute_history$unique_value_valid_from`)
- Indexes: `idx_{table}_{columns}_{filter}` (e.g., `idx_attribute_history_current`)
- Sequences: `{table}_seq` (e.g., `attribute_history_seq`)

### 4. Data Types
- Use appropriate precision: SMALLINT for enums, BIGINT for IDs, DATE for dates
- Avoid TEXT for bounded strings; use VARCHAR(n)
- Use TIMESTAMP for point-in-time data, DATE for day-level temporal data
- Consider JSONB for semi-structured data that won't be queried frequently

### 5. Schema Organization
- Group related tables in schemas (e.g., `mu_raw_lastfm`, `mu`, `mu_quiz`)
- Use consistent schema prefixes for clarity
- Document schema purpose and ownership

### 6. Migration Best Practices
- One logical change per migration file
- Include rollback logic where possible
- Test migrations on production-like data volumes
- Use descriptive names that explain the change


## Pattern Relationships

```
SCD2 Attribute History
    ├─> Uses: Liquibase Migrations (for schema creation)
    ├─> Uses: Coded Enums (for attribute types)
    └─> Complements: Audit Fields (for record-level tracking)

Liquibase Migrations
    ├─> Used by: All database patterns
    └─> Enables: Schema versioning and evolution
```


## Testing Database Patterns

When implementing database patterns, ensure comprehensive testing:

1. **Unit Tests**: Test individual components (e.g., SCD2 merge logic)
2. **Integration Tests**: Test with actual database (use test containers)
3. **Data Quality Tests**: Validate constraints, uniqueness, referential integrity
4. **Performance Tests**: Test with realistic data volumes
5. **Migration Tests**: Test forward and rollback migrations

See [Testing Patterns](../testing/overview.md) for detailed testing strategies.


## See Also

- [Backend Patterns Index](../overview) - All backend patterns
- [Entity Patterns](../entities/overview.md) - JPA entity design patterns
- [API Patterns](../api/conventions.md) - REST API patterns
- [Testing Patterns](../testing/overview.md) - Testing strategies


## Contributing New Database Patterns

When documenting a new database pattern:

1. Use the [PATTERN_TEMPLATE.md](../../doc-templates/PATTERN_TEMPLATE.md) structure
2. Include actual code examples from the codebase
3. Document WHY Art Universe does it this way
4. Provide troubleshooting guidance
5. Add entry to this overview file
6. Update the Backend Patterns Index
