# Art Universe - Services Reference

This document is the **single source of truth** for all services, their ports, and deployment configurations.

The project is currently deployable to a local machine only:
- **Local**: Docker deployment (9xxx ports for main services, 4000 for UI)
- **Production**: Docker deployment (8xxx ports for main services, 3000 for UI)
- **Development**: Individual module development (7xxx ports for main services, 5173 for UI)

## All Services

> **Port Format**: `main / actuator` - The second port (where shown) is the actuator endpoint for monitoring and health checks. 

> ETL services (Calls Generator, Performer, Parser) only expose actuator ports in dev and local environments, not in production.

| Service                     | Purpose | Module | Docs | Dev | Local | Prod |
|-----------------------------|---------|--------|------|-----|-------|------|
| LastFM REST API (Read)      | Read-only REST API for LastFM raw data | `:music:data:raw:lastfm:lastfm-rest-api` | - | 7084 / 7094 | 9084 / 9094 | 8084 / 8094 |
| LastFM ETL REST API (Write) | Write operations REST API for LastFM ETL | `:music:data:raw:lastfm:etl:lastfm-etl-rest-api` | - | 7085 / 7095 | 9085 / 9095 | 8085 / 8095 |
| LastFM Calls Generator      | Generates API calls for LastFM data collection | `:music:data:raw:lastfm:etl:lastfm-calls-generator` | - | - / 7096 | - / 9096 | - / - |
| LastFM Calls Performer      | Executes API calls against LastFM API | `:music:data:raw:lastfm:etl:lastfm-calls-performer` | - | - / 7097 | - / 9097 | - / - |
| LastFM Response Parser      | Parses and processes LastFM API responses | `:music:data:raw:lastfm:etl:lastfm-response-parser` | - | - / 7098 | - / 9098 | - / - |
| Music Data Master           | Curated data management and binding service | `:music:data:master` | [README](../music/data/master/README.md) | 7082 / 7092 | 9082 / 9092 | 8082 / 8092 |
| Music Quiz                  | Quiz generation from approved data | `:music:quiz` | - | 7083 / 7093 | 9083 / 9093 | 8083 / 8093 |
| Music UI                    | React management interface for all services | `:music:ui` | - | 5173 / - | 4000 / - | 3000 / - |
| PostgreSQL (LastFM)         | Database for LastFM raw data (schema: `mu_raw_lastfm`) | N/A | - | 7799 / - | 9999 / - | - / - |
| PostgreSQL (Music Data)     | Database for music data and quiz (schemas: `mu`, `mu_quiz`) | N/A | - | 7789 / - | 9989 / - | - / - |
| Adminer                     | Web-based database administration tool | N/A | - | 7788 / - | 9980 / - | 8880 / - |
| Prometheus                  | Metrics collection and monitoring | N/A | - | 7090 / - | 9090 / - | 8090 / - |
| Grafana                     | Metrics visualization and dashboards | N/A | - | 7000 / - | 9000 / - | 8000 / - |

## Environment Descriptions

### Development (Dev)
Individual module development outside Docker. 
Services run on host machine from IDE or using `./scripts/run-module-dev.sh <module-path>`.

- Vite dev server for UI (hot reload)
- Individual services can be started/stopped independently
- Uses `dev.override.env` for development overrides
- Not all services need to run simultaneously

### Local
Full Docker Compose deployment for local integration testing and development.

- PostgreSQL databases are running in containers

### Production
Production Docker Compose deployment configuration.

- uses external database
- Optimized builds with multi-stage Dockerfiles
- Reduced port exposure (actuator ports typically not exposed)

## Service Dependencies

### Runtime Dependencies
- **Music Data Master** ← depends on PostgreSQL (Music Data)
- **Music Quiz** ← depends on PostgreSQL (Music Data), Music Data API
- **LastFM ETL services** ← depend on PostgreSQL (LastFM)
- **Music UI** ← depends on Music Data API, Music Quiz API, LastFM APIs

### Build Dependencies
See [MODULES.md](MODULES.md) for complete module dependency graph.

## API Endpoints

All REST services follow the pattern: `http://localhost:<port>/api/v1/{resource}`

### Actuator Endpoints
Services with actuator ports expose health and metrics:
- **Health**: `http://localhost:<actuator-port>/actuator/health`
- **Metrics**: `http://localhost:<actuator-port>/actuator/metrics`
- **Prometheus**: `http://localhost:<actuator-port>/actuator/prometheus`

## Database Schemas

| Service | Database | Schema |
|---------|----------|--------|
| LastFM services | `music_universe` | `mu_raw_lastfm` |
| Music Data | `music_universe` | `mu` |
| Music Quiz | `music_universe` | `mu_quiz` |

## Monitoring

### Prometheus
- **Scrape interval**: 15s
- **Targets**: All services with actuator endpoints

### Grafana
- **Data source**: Prometheus
- **Dashboards**: JVM metrics, Spring Boot metrics, custom application metrics

## See Also

- [MODULES.md](MODULES.md) - Complete module reference and build commands
- [DEVELOPMENT.md](DEVELOPMENT.md) - Development workflow and environment setup
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and design patterns
- [env/docker/README.md](../env/docker/README.md) - Docker deployment scripts
