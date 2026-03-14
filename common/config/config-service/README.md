# au-config-service

Centralized runtime configuration service for all Art Universe modules. Stores property values in PostgreSQL and exposes a REST API for registration, retrieval, and live updates.

## Purpose

Allows ETL and application modules to have their tunable parameters (scheduler delays, enable flags, thresholds, rate-limiter settings, etc.) managed centrally without redeployment. Values are persisted in the database and can be updated at runtime via the REST API.

## Key Components

### REST API

**Base path**: `/api/v1/config/properties`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/register` | Idempotent bulk registration — creates property with default if absent, always returns current value |
| `GET` | `/` | Fetch all registered properties |
| `GET` | `/{key}` | Fetch single property by key |
| `PUT` | `/{key}` | Update current value (validated against type and constraints) |

**Components**: [ConfigPropertyController.java](src/main/java/yurykorzun/art/universe/common/config/service/controller/ConfigPropertyController.java)

### Persistence

Each property is stored as a `ConfigPropertyEntity` row with:
- `key` — unique string key (e.g. `lastfm.generator.schedule.delay-secs`)
- `propertyType` — `INTEGER`, `BOOLEAN`, `DECIMAL`, `STRING`
- `defaultValue` — used on first registration
- `currentValue` — the live value (initially equal to `defaultValue`)
- `description` — human-readable label
- `constraints` — optional JSON: `{"min": 1, "max": 3600}` or `{"allowedValues": [...]}`

**Components**: [ConfigPropertyEntity.java](src/main/java/yurykorzun/art/universe/common/config/service/entity/ConfigPropertyEntity.java), [ConfigPropertyRepository.java](src/main/java/yurykorzun/art/universe/common/config/service/repository/ConfigPropertyRepository.java)

### Constraint Validation

Before persisting a `PUT` update, `ConstraintValidator` checks the new value against the stored constraints and throws a `400 Bad Request` if violated.

**Components**: [ConstraintValidator.java](src/main/java/yurykorzun/art/universe/common/config/service/service/ConstraintValidator.java)

## Configuration

| Property | Description |
|----------|-------------|
| `AU_CONFIG_DB_SCHEMA` | PostgreSQL schema (default: `au_config`) |
| `AU_CONFIG_DB_USERNAME` | DB user (default: `au_config_dm`) |
| `AU_CONFIG_DB_PASSWORD_DM` | DB password |
| `AU_CONFIG_APP_INTERNAL_PORT` | Server port (default: `8080`) |

Database connection uses standard `AU_DB_MASTER_HOST`, `AU_DB_MASTER_PORT`, `AU_DB_NAME` variables from the common env.

## Database Migration

Managed by the `config-service-liquibase-resources` module. Run separately via the `au-config-liquibase` Docker service before starting the application.

## See Also

- [config-client README](../config-client/README.md) — how consumer modules connect to this service
- [Centralized Configuration Pattern](../../../docs/kb/patterns/backend/configuration/centralized-configuration.md) — full pattern documentation
