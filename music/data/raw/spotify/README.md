# Spotify Modules

This directory contains modules for Spotify raw data collection and ETL pipeline.

## Module Index

### Shared Spotify Modules

- [Spotify Models](spotify-models/README.md) - JPA entities and DTOs for Spotify data
- [Spotify Repositories](spotify-repositories/README.md) - JPA repositories for Spotify entities

### DB Schema Migration Modules

- [Liquibase Resources](migrations/spotify-liquibase-resources/README.md) - Liquibase changelog resources
- [Liquibase Service](migrations/spotify-liquibase-service/README.md) - Database migration service

### ETL Pipeline Modules

For detailed ETL pipeline architecture and data flow, see **[Spotify ETL Pipeline Documentation](etl/README.md)**.

- [Calls Generator](etl/spotify-calls-generator/README.md) - Stage 1: Generate API call tasks for entity discovery and search
- [Calls Performer](etl/spotify-calls-performer/README.md) - Stage 2: Execute Spotify API calls and store raw responses
- [Response Parser](etl/spotify-response-parser/README.md) - Stage 3: Parse responses, stage data, and score search matches
- [Staging Applicator](etl/spotify-staging-applicator/README.md) - Stage 4: Apply staging data to target tables and reconcile bindings

### REST API Modules

- [Spotify REST API](spotify-rest-api/README.md) - REST API for seeding Spotify entities

## Related Documentation

- [Project Modules Index](../../../../docs/MODULES.md)
