# LastFM Modules

This directory contains documentation for LastFM raw data collection and ETL pipeline modules.

## Module Index

### Shared LastFM Modules

- [LastFM Models](lastfm-models/README.md) - JPA entities and DTOs for LastFM data
- [LastFM Repositories](lastfm-repositories/README.md) - JPA repositories for LastFM entities
- [Tasks Coordinator](etl/lastfm-tasks-coordinator/README.md) - Coordinate DB tasks during maintenance

### DB Schema migration modules

- [Liquibase Resources](migrations/lastfm-liquibase-resources/README.md) - Liquibase changelog resources
- [Liquibase Service](migrations/lastfm-liquibase-service/README.md) - Database migration service

### ETL Pipeline Modules

For detailed ETL pipeline architecture and data flow, see **[Lastfm ETL Pipeline Documentation](etl/README.md)**.

- [Calls Generator](etl/lastfm-calls-generator/README.md) - Stage 1: Generate API call tasks based on data staleness
- [Calls Performer](etl/lastfm-calls-performer/README.md) - Stage 2: Execute API calls and store raw responses
- [Response Parser](etl/lastfm-response-parser/README.md) - Stage 3: Parse responses and update entities

### REST API Modules

- [LastFM REST API](lastfm-rest-api/README.md) - REST API for LastFM data (read-only)
- [ETL REST API](etl/lastfm-etl-rest-api/README.md) - REST API for manual ETL control and entity editing

### Shared Test Support Modules

- [LastFM Commons Test](test/lastfm-commons-test/README.md) - General LastFM test utilities
- [LastFM Commons Test Context](test/lastfm-commons-test-context/README.md) - Spring context test utilities
- [LastFM Commons Test JPA](test/lastfm-commons-test-jpa/README.md) - JPA test utilities
- [LastFM Commons Test DB](test/lastfm-commons-test-db/README.md) - Database test utilities
- [LastFM Commons Test DB Helper](test/lastfm-commons-test-db-helper/README.md) - Database test helpers
- [LastFM Commons Test Web](test/lastfm-commons-test-web/README.md) - Web test utilities


## Related documentation

- [Project Modules Index](../../../../docs/MODULES.md)
