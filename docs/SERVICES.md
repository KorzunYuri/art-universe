# Art Universe - Services Reference

This document is the **single source of truth** for all services, their ports, and deployment configurations.

The project supports multiple deployment modes:
- **Development**: Individual module development (7xxx ports for main services, 5173 for UI)
- **Local (Docker Compose)**: Full stack via Docker Compose (9xxx ports for main services, 4000 for UI)
- **Local (Kubernetes)**: Full stack via Kustomize (9xxx ports for main services, 4000 for UI)
- **Production (Docker Compose)**: Docker Compose deployment (8xxx ports for main services, 3000 for UI)
- **Production (Kubernetes)**: Kustomize with external DBs (8xxx ports for main services, 3000 for UI)

## All Services

> **Port Format**: Web services (REST APIs) expose both application and actuator endpoints on the same port. Non-web ETL services expose only actuator endpoints.

> ETL services (Calls Generator, Performer, Parser) are non-web applications that only expose actuator endpoints in dev and local environments, not in production.

| Service                                                                                                 | Purpose                                                     | Module | Dev  | Local | Prod |
|---------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|--------|------|-------|------|
| [LastFM REST API (Read)](../music/data/raw/lastfm/lastfm-rest-api/README.md)                            | Read-only REST API for LastFM raw data                      | [`:music:data:raw:lastfm:lastfm-rest-api`](../music/data/raw/lastfm/lastfm-rest-api/README.md) | 7084 | 9084  | 8084 |
| [LastFM ETL REST API (Write)](../music/data/raw/lastfm/etl/lastfm-etl-rest-api/README.md)               | Write operations REST API for LastFM ETL                    | [`:music:data:raw:lastfm:etl:lastfm-etl-rest-api`](../music/data/raw/lastfm/etl/lastfm-etl-rest-api/README.md) | 7085 | 9085  | 8085 |
| [LastFM Calls Generator](../music/data/raw/lastfm/etl/lastfm-calls-generator/README.md)                 | Generates API calls for LastFM data collection              | [`:music:data:raw:lastfm:etl:lastfm-calls-generator`](../music/data/raw/lastfm/etl/lastfm-calls-generator/README.md) | 7086 | 9086  | 8086 |
| [LastFM Calls Performer](../music/data/raw/lastfm/etl/lastfm-calls-performer/README.md)                 | Executes API calls against LastFM API                       | [`:music:data:raw:lastfm:etl:lastfm-calls-performer`](../music/data/raw/lastfm/etl/lastfm-calls-performer/README.md) | 7087 | 9087  | 8087 |
| [LastFM Response Parser](../music/data/raw/lastfm/etl/lastfm-response-parser/README.md)                 | Parses and processes LastFM API responses                   | [`:music:data:raw:lastfm:etl:lastfm-response-parser`](../music/data/raw/lastfm/etl/lastfm-response-parser/README.md) | 7088 | 9088  | 8088 |
| Spotify REST API                                                                                        | REST API for Spotify raw data + seed endpoints              | `:music:data:raw:spotify:spotify-rest-api` | 7094 | 9094  | 8094 |
| Spotify Calls Generator                                                                                 | Generates API calls for Spotify data collection             | `:music:data:raw:spotify:etl:spotify-calls-generator` | 7091 | 9091  | 8091 |
| Spotify Calls Performer                                                                                 | Executes API calls against Spotify API                      | `:music:data:raw:spotify:etl:spotify-calls-performer` | 7092 | 9092  | 8092 |
| Spotify Response Parser                                                                                 | Parses Spotify API responses and writes to staging tables   | `:music:data:raw:spotify:etl:spotify-response-parser` | 7093 | 9093  | 8093 |
| Spotify Staging Applicator                                                                              | Applies sealed staging iterations to target tables          | `:music:data:raw:spotify:etl:spotify-staging-applicator` | 7095 | 9095  | 8095 |
| [Music Semantic Analyzer](../music/data/semantic/semantic-analyzer/README.md)                           | LLM-powered semantic analysis of music entities             | [`:music:data:semantic:semantic-analyzer`](../music/data/semantic/semantic-analyzer/README.md) | 7096 | 9096  | 8096 |
| [Music Semantic Analyzer Ticket Intake Service](../music/data/semantic/ticket-intake-service/README.md) | REST API for analysis ticket submission (Go)                | [`:music:data:semantic:ticket-intake-service`](../music/data/semantic/ticket-intake-service/README.md) | 7095 | 9095  | 8095 |
| [Art Data Master](../art/data/master/README.md)                                                         | Art foundation data management (Person CRUD)                | [`:art:data:master`](../art/data/master/README.md) | 7081 | 9081  | 8081 |
| [Music Data Master](../music/data/master/README.md)                                                     | Curated data management and binding service                 | [`:music:data:master`](../music/data/master/README.md) | 7082 | 9082  | 8082 |
| [Music Quiz](../music/quiz/README.md)                                                                   | Quiz generation from approved data                          | [`:music:quiz`](../music/quiz/README.md) | 7083 | 9083  | 8083 |
| [Art Universe UI](../ui/README.md)                                                                      | React management interface for all services                 | [`:ui`](../ui/README.md) | 5173 | 4000  | 3000 |
| PostgreSQL (LastFM + Spotify)                                                                           | Database for raw data (schemas: `mu_raw_lastfm`, `mu_raw_spotify`) | N/A | 7799 | 9999  | -    |
| PostgreSQL (Music Data)                                                                                 | Database for music data and quiz (schemas: `mu`, `mu_quiz`) | N/A | 7789 | 9989  | -    |
| Prometheus                                                                                              | Metrics collection and monitoring                           | N/A | 7080 | 9090  | 8080 |
| Grafana                                                                                                 | Metrics visualization and dashboards                        | N/A | 7000 | 9000  | 8000 |
| Zipkin                                                                                                  | Traces storage and visualization                            | N/A | 7411 | 9411  | 8411 |

## Environment Descriptions

### Development (Dev)
Individual module development outside Docker. 
Services run on host machine from IDE or using `./scripts/run-module-dev.sh <module-path>`.

- Vite dev server for UI (hot reload)
- Individual services can be started/stopped independently
- Uses `dev.override.env` for development overrides
- Not all services need to run simultaneously

### Local (Docker Compose)
Full Docker Compose deployment for local integration testing and development.

- PostgreSQL databases are running in containers

### Local (Kubernetes)
Full Kustomize deployment with containerized databases. Same ports as Docker Compose local.

- PostgreSQL StatefulSets in the cluster
- See [Kubernetes Deployment](../env/k8s/README.md) for details

### Production (Docker Compose)
Production Docker Compose deployment configuration.

- Uses external database
- Optimized builds with multi-stage Dockerfiles
- Reduced port exposure (actuator ports typically not exposed)

### Production (Kubernetes)
Kustomize deployment connecting to external databases.

- ExternalName services for database connectivity (configurable host)
- Same application images as local K8s overlay
- See [Kubernetes Deployment](../env/k8s/README.md) for details

## API Endpoints

All REST services follow the [same pattern](kb/patterns/backend/api/conventions.md).

### Actuator Endpoints
All services expose actuator endpoints for health and metrics:
- **Web services**: Actuator on same port as application
- **Non-web ETL services**: Actuator on dedicated port

Available endpoints:
- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## Monitoring

### Prometheus
- **Scrape interval**: 15s
- **Targets**: All services with actuator endpoints

### Grafana
- **Data source**: Prometheus
- **Dashboards**: JVM metrics, Spring Boot metrics, custom application metrics

## See Also

- [Modules reference](MODULES.md) - Complete module reference and build commands
- [Development reference](DEVELOPMENT.md) - Development workflow and environment setup
- [Architecture reference](ARCHITECTURE.md) - System architecture and design patterns
- [Docker Deployment reference](../env/docker/README.md) - Docker Compose deployment scripts
- [Kubernetes Deployment reference](../env/k8s/README.md) - Kubernetes/Kustomize deployment
