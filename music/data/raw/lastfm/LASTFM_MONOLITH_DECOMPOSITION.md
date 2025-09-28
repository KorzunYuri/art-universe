# LastFM Monolith Refactoring Guide

## Overview

Decomposition of `lastfm-monolith` into separate microservices to improve scalability, deployment, and maintenance.

## Target Architecture

### Module Structure

```
lastfm/
├── models/                          # Common data models
├── etl/
│   ├── calls-generator/            # API calls generation
│   ├── calls-performer/            # API calls execution
│   ├── response-parser/            # API responses parsing
│   └── rest-api/                   # Data management (approval, maintenance)
├── migrations/
│   ├── liquibase-resources/        # Migration resources (XML changelogs)
│   └── liquibase-service/          # Migration deployment service
├── rest-api/                       # REST API for UI (read-only)
└── test-common/                    # Common test utilities
```

### Module Dependencies

```
migrations/liquibase-resources (root)
    ↓
models
    ↓
├── etl/calls-generator
├── etl/calls-performer  
├── etl/response-parser
├── etl/rest-api
├── rest-api
└── test-common
```

### Database Architecture

- **Master DB**: Data writes (ETL modules)
- **Replica DB**: Data reads (rest-api, UI)
- **Physical Replication**: PostgreSQL streaming replication

## Module Responsibilities

### models
- JPA entities (LastfmArtist, LastfmTrack, etc.)
- DTOs for API
- **Dependencies**: migrations/liquibase-resources
- **Note**: Repositories are module-specific, not in models

### etl/calls-generator
- `LastfmApiCallGenerator` - call generation logic
- Schedulers for creating API calls
- Own repositories for API calls
- **DB Access**: Master (write)

### etl/calls-performer
- `LastfmApiClientImpl` - HTTP client with rate limiting
- API calls execution to LastFM
- Response storage
- Own repositories for responses
- **DB Access**: Master (write)

### etl/response-parser
- `LastfmApiResponseProcessor` - response processing
- `EntityFactory` - DTOs to entities mapping
- `EntityAttributeHandler` - SCD2 tracking
- Own repositories for entities
- **DB Access**: Master (write)

### etl/rest-api
- Approval workflow endpoints
- Maintenance operations
- Entity management API
- Own repositories for management
- **DB Access**: Master (write/read)

### migrations/liquibase-resources
- XML changelogs
- Database schema definitions
- **No runtime dependencies**

### migrations/liquibase-service
- Standalone migration service
- Migration deployment
- **DB Access**: Master (DDL)

### rest-api
- Read-only API for UI
- Entity search and filtering
- Own repositories for read operations
- **DB Access**: Replica (read-only)
- **Dependencies**: models only

### test-common
- Testcontainers setup
- Common test utilities
- **Dependencies**: models

## Deployment Architecture

### Docker Containers

```
ETL Modules (Master DB):
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  calls-generator│    │ calls-performer │    │ response-parser │
│     (ETL-1)     │    │     (ETL-2)     │    │     (ETL-3)     │
│   [Master DB]   │    │   [Master DB]   │    │   [Master DB]   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   etl/rest-api  │
                    │     (ETL-4)     │
                    │   [Master DB]   │
                    └─────────────────┘

Other Services:
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   rest-api      │    │ liquibase-svc   │    │   Master DB     │
│   (Read API)    │    │  (Migrations)   │    │   PostgreSQL    │
│  [Replica DB]   │    │   [Master DB]   │    └─────────────────┘
└─────────────────┘    └─────────────────┘             │
         │                       │                     │ replication
         │                       │                     ▼
┌─────────────────┐              │              ┌─────────────────┐
│      UI         │              │              │   Replica DB    │
│   (External)    │              │              │   PostgreSQL    │
└─────────────────┘              │              └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Test Env      │
                    │ (test-common)   │
                    │  [Test DB]      │
                    └─────────────────┘
```

### Service Communication

```
API Flow:
LastFM API ← calls-performer ← calls-generator
                │
                ▼
         Master DB (write)
                │
                ▼ (processing)
    response-parser → Master DB
                │
                ▼ (replication)
           Replica DB ← rest-api ← UI

Management Flow:
Admin UI → etl/rest-api → Master DB
                │
                ▼ (replication)
           Replica DB
```

## Migration Strategy

### Phase 1: Infrastructure
1. Setup separate Docker containers
2. Configure database connections (master/replica)
3. Deploy liquibase-service

### Phase 2: Core Modules
1. Extract models module
2. Extract migrations/liquibase-resources
3. Update dependencies

### Phase 3: ETL Pipeline
1. Extract calls-generator
2. Extract calls-performer
3. Extract response-parser
4. Extract etl/rest-api

### Phase 4: Read API
1. Extract rest-api module
2. Configure replica DB access
3. Update UI connections

### Phase 5: Testing
1. Extract test-common
2. Update integration tests
3. End-to-end testing

## Configuration

### Environment Variables per Service

**ETL Services** (Master DB):
- `MURAW_LASTFM_DB_MASTER_HOST`
- `MURAW_LASTFM_DB_MASTER_PASSWORD`

**Read API** (Replica DB):
- `MURAW_LASTFM_DB_REPLICA_HOST`
- `MURAW_LASTFM_DB_REPLICA_PASSWORD`

**Calls Performer**:
- `MURAW_LASTFM_API_KEY`

### Docker Compose Structure

```yaml
services:
  calls-generator:
    image: lastfm-calls-generator
    environment:
      - DB_HOST=${MASTER_DB_HOST}
  
  calls-performer:
    image: lastfm-calls-performer
    environment:
      - DB_HOST=${MASTER_DB_HOST}
      - LASTFM_API_KEY=${API_KEY}
  
  response-parser:
    image: lastfm-response-parser
    environment:
      - DB_HOST=${MASTER_DB_HOST}
  
  etl-rest-api:
    image: lastfm-etl-rest-api
    environment:
      - DB_HOST=${MASTER_DB_HOST}
  
  rest-api:
    image: lastfm-rest-api
    environment:
      - DB_HOST=${REPLICA_DB_HOST}
  
  liquibase-service:
    image: lastfm-liquibase-service
    environment:
      - DB_HOST=${MASTER_DB_HOST}
```

## Benefits

- **Scalability**: Independent component scaling
- **Deployment**: Separate module releases
- **Database Load**: Read/write load separation
- **Testing**: Isolated module testing
- **Maintenance**: Simplified maintenance
