# Art Universe - Architecture Overview

## Module Dependencies

> **Note**: See **[MODULES.md](MODULES.md)** for complete module listing.

### Data Flow
1. **Raw Data Modules** collect 'raw' data (artists, albums, tracks etc. and their attributes) from external APIs:
   1. implemented: LastFM
   2. planned: Spotify, MusicBrainz, etc.
2. **Music Data Master** manages 'master' entities and bindings of 'raw' data from external APIs to them
3. **Music Quiz** generates quizzes from master entities
   1. implemented: single subset of master entities approved by admin as the only datasource
   2. planned: subsets of master entities defined by different users
4. **UI** provides unified management interface for all three modules

### UI Module Responsibilities
- **Raw Data Administration**: Approve/decline entities, manage API collection settings
- **Curated Data Management**: Create bindings, manage approved entity relationships  
- **Quiz Configuration**: Set up quiz parameters, manage quiz-specific entity selections

## Data Architecture

### Entity Hierarchy
- **Raw Entities**: Direct API mappings with full attribute history
- **Master Entities**: Curated 'single source of truth'-entities with manual approval workflow  
- **Quiz Entities**: Approved subset specifically marked for quiz use, user-defined subsets in the future

### Entity Relationships
- External entities can be bound to master entities (name unification & correction): for example, artist "Lyapis Trubetskoy" from Spotify should be bound to master artist "Ляпис Трубецкой")
- Master entities can be bound to other master entities (artist A is a featuring artist on track T, track X is a remix of track Y etc.)

## Database Schema Design
Data are persisted in a single Postgres instance with the following databases:
- `mu`: Master data with bindings
- `mu_quiz`: Quiz-specific data and configurations
- `mu_raw_lastfm`: LastFM raw data with full API responses

## Common Patterns

For common patterns used in the project, see **[Patterns Reference](kb/patterns/README.md)**.

## Security Architecture

### Authentication & Authorization
- Currently: No authentication
- Planned: role-based access, API GW with JWT-based authentication / OAuth 2.0

### Data Protection
- Secrets management via environment files
- API keys stored in `.secrets.env` files
- Database credentials separated by environment

## Monitoring and Observability

### Current Implementation

The system includes integrated monitoring using Prometheus and Grafana for metrics collection and visualization.

#### Prometheus
- **Purpose**: Metrics collection and time-series storage
- **Scrape Interval**: 15 seconds
- **Targets**: All Spring Boot services via actuator endpoints
- **Metrics Collected**:
  - JVM metrics (memory, threads, garbage collection)
  - Spring Boot metrics (HTTP requests, response times)
  - Database connection pool metrics
  - Custom application metrics (number of created entities and others)

#### Grafana
- **Purpose**: Metrics visualization and dashboards
- **Data Source**: Prometheus
- **Dashboards**: JVM metrics, Spring Boot metrics, custom application metrics

#### Actuator Endpoints
All Spring Boot services expose actuator endpoints for monitoring:
- **Health**: `/actuator/health` - Service health status
- **Metrics**: `/actuator/metrics` - Available metrics
- **Prometheus**: `/actuator/prometheus` - Prometheus-formatted metrics

See **[SERVICES.md](SERVICES.md)** for actuator port configurations.

### Logging
- **Format**: Structured logging with correlation IDs (planned)
- **Output**: Console logs (captured by Docker)
- **Future**: Centralized log aggregation (ELK stack or similar)

## Scalability Considerations

### Current Limitations
- Single-instance deployment
- Synchronous API processing
- Manual approval workflow

### Planned Improvements
- **Kafka Integration**: Asynchronous message processing
- **Structured Logging**: JSON logs with correlation IDs
- **Centralized Logging**: ELK stack or similar for log aggregation
- **Caching Layer**: Redis for frequently accessed data

### Deployment Options
- **Docker Compose**: Local and production modes. See [Docker Deployment](../env/docker/README.md).
- **Kubernetes (Kustomize)**: Local (with containerized DBs) and prod (with external DBs) overlays. See [Kubernetes Deployment](../env/k8s/README.md).
