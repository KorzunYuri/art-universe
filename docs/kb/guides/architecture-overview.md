# Architecture Overview

LLM-optimized quick reference. For complete details, see [Architecture Reference](../../ARCHITECTURE.md)

This guide provides a high-level overview of the Art Universe system architecture, optimized for LLM consumption.


## Project Description

**Art Universe** is a platform for collecting, curating, and utilizing data from the art domain. The project currently focuses on music data and quiz applications.


## Three-Stage Pipeline

The system follows a three-stage architecture:

- Raw Data Collection (Lastfm, Spotify etc.)
- Master Data Curation
- Application  (Quizzes)

### 1. Data Collection

Automated ETL processes gather raw data from external APIs:
- **Current**: LastFM API, Spotify API

Raw data is stored in source-specific schemas (e.g., `mu_raw_lastfm`)

### 2. Master Data Curation

Raw data from multiple sources is:
- Connected via **Entity Binding**
- Reviewed via **Approval Workflow**
- Curated into unified master database (`mu` schema)

### 3. Applications

Curated data powers various applications:
- **Current**: "Guess the Track" quiz game
- **Planned**: Additional quiz types, analytics, recommendations


## Current Implementation

### Domains

- [LastFM Data Collection](../../../music/data/raw/lastfm/README.md): Complete ETL pipeline for fetching and parsing LastFM API data
- [Spotify Data Collection](../../../music/data/raw/spotify/README.md): Complete ETL pipeline for fetching and parsing Spotify Web API data
- [Master Data](../../../music/data/master/music-master-rest-api/README.md): Entity management for artists, albums, tracks with basic relationships
- [Quiz Game](../../../music/quiz/README.md): "Guess the Track" quiz with configurable difficulty and categories

[Art Universe UI](../../../ui/README.md) provides the interface to put all data management and application together.

### Current Data Flow

#### Stage 1: Raw Data Collection (LastFM and Spotify ETL)

1. **Calls Generator** - Generates API calls based on seed data
2. **Calls Performer** - Executes API calls against LastFM / Spotify and stores responses for parsing
3. **Response Parser** - Parses entities from responses and stores them in target tables (Lastfm) or staging layer (Spotify)
4. **Staging Applicator** - Picks up batches of entities from staging and applies them to target tables

#### Stage 2: Master Data Curation

1. **Entity Binding** - Links raw entities to master entities
2. **Approval Workflow** - Reviews and approves entities (PENDING → APPROVED)
3. **Master Data** - Approved entities available in `mu` schema

#### Stage 3: Quiz Generation

1. **Pipeline** - Processes approved entities
2. **Quiz Data** - Generated quiz questions in `mu_quiz` schema
3. **UI** - React application presents quizzes to users

### Planned Features

- **Enhanced Master Data**: Support for complex entity relationships with connection types (e.g., track versions, artist collaborations)
- **Spotify Integration**: Raw ETL complete; attributes ETL to enrich master data is next
- **Extended Quiz Types**: Multiple quiz variations and game modes
- **Additional Domains**: Expansion beyond music to visual arts and literature


## Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Primary language |
| **Spring Boot** | 3.4.3 | Application framework |
| **Spring Dependency Management** | 1.1.7 | Dependency management |
| **PostgreSQL** | 14 | Database |
| **Liquibase** | 4.15.0 | Database migrations |
| **JUnit** | 5.10.0 | Testing framework |
| **Mockito** | Latest | Mocking framework |
| **TestContainers** | 1.20.4/1.19.6 | Integration testing |
| **Lombok** | 1.18.30 | Boilerplate reduction |
| **Hypersistence Utils** | 3.9.3 | JPA utilities |
| **Springdoc OpenAPI** | 2.8.6 | API documentation |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.1.0 | UI framework |
| **TypeScript** | 5.8.3 | Type-safe JavaScript |
| **Vite** | 6.3.5 | Build tool |
| **React Router** | 6.30.0 | Routing |
| **TanStack Query** | 5.28.0 | Data fetching & state management |
| **Axios** | 1.12.0 | HTTP client |
| **XYFlow React** | 12.3.2 | Pipeline diagrams |
| **Dagre** | 0.8.5 | Graph layout |
| **Sass** | 1.89.0 | CSS preprocessor |

### Build & Deployment

| Technology | Purpose |
|------------|---------|
| **Gradle** | Multi-project build system (Wrapper only at project root) |
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **Prometheus** | Metrics collection |
| **Grafana** | Metrics dashboards |


## Module Structure

**For complete module listing**: See [Project Modules Index](../../MODULES.md)


## Database Architecture

### Database Deployment

- **Dev & Local**: Containerized PostgreSQL via Docker Compose
- **Production**: External databases on host machine

### Schema Separation

| Schema | Purpose | Owner Module |
|--------|---------|--------------|
| **`mu`** | Curated master data | Music Data Master |
| **`mu_raw_lastfm`** | Raw LastFM data | LastFM modules |
| **`mu_quiz`** | Quiz-specific data | Music Quiz |

### Migration Strategy

- **Liquibase** manages all schema changes
- Migrations run automatically on application startup
- Each module maintains its own migrations

**See**: [liquibase pattern](../patterns/backend/database/liquibase.md)


## Service Ports

**For complete port reference**: See [Services Reference](../../SERVICES.md)

### Quick Reference

| Service | Dev | Local | Production |
|---------|-----|-------|------------|
| **LastFM REST API (Read)** | :7081 | :9084 | :8081 |
| **LastFM ETL API (Write)** | :7085 | :9085 | :8085 |
| **Music Data** | :7082 | :9082 | :8082 |
| **Music Quiz** | :7083 | :9083 | :8083 |
| **Music UI** | :5173 | :4000 | :3000 |
| **Prometheus** | :7090 | :9090 | :8090 |
| **Grafana** | :7000 | :9000 | :8000 |

**Note**: All Spring Boot services expose Spring Boot Actuator endpoints on separate ports (configured per environment).


## Deployment and Environments

### Running the Project with Docker Compose

Located in `env/docker/`:

- `deploy.sh/.bat <local|prod>` - Deploy environment
- `stop.sh/.bat <local|prod|all>` - Stop containers
- `cleanup.sh/.bat <local|prod|all>` - Cleanup containers and images

See [Docker Deployment Reference](../../../env/docker/README.md) for the details.

### Monitoring and Observability

- **Spring Boot Actuator**: All services expose health, metrics, and operational endpoints
- **Prometheus**: Collects metrics from all services
- **Grafana**: Visualizes metrics with pre-configured dashboards

Configuration files:
- Prometheus: `env/docker/common/prometheus/`
- Grafana: `music/data/env/docker/compose/dev__mu/grafana/`


## API Architecture

### REST Conventions

- **Endpoint pattern**: `/api/v1/{entity}`
- **CORS**: Enabled globally
- **Versioning**: All services use `v1`
- **Health checks**: `/health` endpoint via Actuator

**See**: [API conventions](../patterns/backend/api/conventions.md)

### Service Boundaries

- **LastFM REST API (Read)**: Read-only access to raw LastFM data
- **LastFM ETL API (Write)**: Managing entities out of ETL scope
- **Music Data**: Curated music data CRUD operations
- **Music Quiz**: Quiz generation and retrieval


## Design Decisions

### Why Entity Binding?

- **Problem**: Multiple data sources have overlapping entities
- **Solution**: Bind raw entities to single master entity
- **Benefit**: Unified view across all data sources

**See**: @features/entity-binding/

### Why Approval Workflow?

- **Problem**: Raw data quality varies
- **Solution**: Manual review before promotion to master
- **Benefit**: High-quality curated data

**See**: @features/approval-workflow/ (planned)

### Why Coded Enums?

- **Problem**: String enums waste space and are error-prone
- **Solution**: Integer codes with type-safe enum mapping
- **Benefit**: Database efficiency + type safety

**See**: @patterns/backend/entities/coded-enums.md

### Why Separate Schemas?

- **Problem**: Mixing raw and curated data causes confusion
- **Solution**: Separate schemas for raw, master, and application data
- **Benefit**: Clear data boundaries and ownership

### Why Three-Stage ETL?

- **Problem**: Direct source-to-application coupling
- **Solution**: Collection → Curation → Application stages
- **Benefit**: Flexibility, data quality, multiple applications


## Quick Navigation

| I want to... | Go to... |
|--------------|----------|
| **Understand a feature** | @features/ |
| **Learn implementation patterns** | @patterns/ |
| **Work on a specific module** | @modules/ |
| **See complete architecture details** | `docs/ARCHITECTURE.md` |
| **Understand data flow** | `docs/ARCHITECTURE.md` |
| **Find service ports** | `docs/SERVICES.md` |
| **See all modules** | `docs/MODULES.md` |


## See Also

- **Main Documentation**:
  - `docs/ARCHITECTURE.md` - Complete architectural details
  - `docs/MODULES.md` - Complete module listing
  - `docs/SERVICES.md` - Service ports and endpoints
  - `docs/DEVELOPMENT.md` - Development guide

- **Claude Documentation**:
  - **@features/** - Feature documentation
  - **@modules/** - Module-specific docs
  - **@patterns/** - Implementation patterns
  - **@guides/development-tasks.md** - Development workflows
