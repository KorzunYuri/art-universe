# Spotify Liquibase Service

Spring Boot service that executes Liquibase database migrations for the `mu_raw_spotify` schema using the changelogs defined in [Spotify Liquibase Resources](../spotify-liquibase-resources/README.md).

This is a short-lived init container that runs migrations and exits. In Docker Compose deployments, ETL services depend on this service completing successfully.

## Related Documentation

- [Spotify Liquibase Resources](../spotify-liquibase-resources/README.md) - Changelog files applied by this service
- [Spotify Modules Overview](../../README.md) - Overview of all Spotify modules
- [Project Modules Index](../../../../../../docs/MODULES.md) - Return to main modules index
