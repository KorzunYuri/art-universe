# Observability Infrastructure

This document describes the observability setup for the Art Universe platform, covering distributed tracing, metrics collection, and monitoring.

## Stack Overview

The observability infrastructure consists of:

- **Micrometer Observability**: Spring Boot 3.4+ native observability framework
- **Micrometer Tracing**: Distributed tracing abstraction with OpenTelemetry bridge
- **Zipkin**: Trace collection and visualization backend (uses Elasticsearch storage)
- **Prometheus**: Metrics collection and time-series storage
- **Grafana**: Metrics visualization and dashboards
- **Spring Boot Actuator**: Endpoint exposure for health checks and metrics

See [commons-observability README](../common/commons-observability/README.md) for details.

## What is Observed

### Implicit Automatic Observations

These are traced automatically by Spring Boot without explicit annotations:

#### 1. HTTP Requests (All REST APIs)
- **Observer**: `ServerHttpObservationFilter` (Spring Web)
- **Span Name**: `HTTP {METHOD} {endpoint-pattern}`
- **Tags**:
  - `http.method`: GET, POST, etc.
  - `http.status_code`: 200, 404, 500, etc.
  - `http.url`: Request URI
  - `outcome`: SUCCESS, CLIENT_ERROR, SERVER_ERROR
- **Exclusions**: `/actuator/**` endpoints

#### 2. Database Queries (All JPA Operations)
- **Observer**: `HibernateStatisticsMetrics` + `DataSourcePoolMetrics`
- **Span Name**: `{Repository}.{method}` or `SELECT/INSERT/UPDATE/DELETE`
- **Tags**:
  - `repository`: Repository class name
  - `method`: Repository method name
- **Metrics**:
  - `spring.data.repository.invocations`: Method-level timing
  - `hikaricp.connections.active`: Active DB connections
  - `hibernate.sessions.open`: Open Hibernate sessions
- **Enabled by**: `hibernate.generate_statistics: true` in [commons-observability module](../common/commons-observability/src/main/resources/commons-observability.yml)

#### 3. RestClient/RestTemplate Outbound Calls
- **Observer**: `ClientHttpObservationInterceptor` (Spring Web)
- **Span Name**: `HTTP {METHOD}`
- **Behavior**: Propagates trace context via B3 headers (`X-B3-TraceId`, `X-B3-SpanId`)

### Explicit Custom Observations

- **@Observed** Annotated Methods
- **AOP-Based** Aspects, e.g.:
  - [ApiCallGeneratorObservabilityAspect.java (Lastfm)](../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/aspect/ApiCallGeneratorObservabilityAspect.java)
  - [ApiCallPerformerObservabilityAspect.java (Lastfm)](../music/data/raw/lastfm/etl/lastfm-calls-performer/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/perform/aspect/ApiCallPerformerObservabilityAspect.java)
  - [ApiResponseProcessorObservabilityAspect.java (Lastfm)](../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/aspect/ApiResponseProcessorObservabilityAspect.java)
  - [ApiCallGeneratorObservabilityAspect.java (Spotify)](../music/data/raw/spotify/etl/spotify-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/task/call/generate/aspect/ApiCallGeneratorObservabilityAspect.java)
  - [ApiCallPerformerObservabilityAspect.java (Spotify)](../music/data/raw/spotify/etl/spotify-calls-performer/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/task/call/perform/aspect/ApiCallPerformerObservabilityAspect.java)
  - [ApiResponseProcessorObservabilityAspect.java (Spotify)](../music/data/raw/spotify/etl/spotify-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/task/response/process/aspect/ApiResponseProcessorObservabilityAspect.java)
  - [StagingApplicationObservabilityAspect.java (Spotify)](../music/data/raw/spotify/etl/spotify-staging-applicator/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/application/aspect/StagingApplicationObservabilityAspect.java)

List of observed/timed methods by module:

- [lastfm-calls-generator](../music/data/raw/lastfm/etl/lastfm-calls-generator/README.md)
  - batch API calls generation per method
- [lastfm-calls-performer](../music/data/raw/lastfm/etl/lastfm-calls-performer/README.md)
  - batch API call processing
  - single API call processing
  - single API call attempt
- [lastfm-response-parser](../music/data/raw/lastfm/etl/lastfm-response-parser/README.md)
  - batch response processing
  - single response processing (per method) (timer)
- [Lastfm ETL REST API](../music/data/raw/lastfm/etl/lastfm-etl-rest-api/README.md)
  - database maintenance tasks
- [spotify-calls-generator](../music/data/raw/spotify/etl/spotify-calls-generator/README.md)
  - single API call generation per call type (timer: `music.data.raw.spotify.api.call.generate`, tags: `api_call_type`, `status`)
- [spotify-calls-performer](../music/data/raw/spotify/etl/spotify-calls-performer/README.md)
  - single API call execution per call type (timer: `music.data.raw.spotify.api.call.perform`, tags: `api_call_type`, `status`)
- [spotify-response-parser](../music/data/raw/spotify/etl/spotify-response-parser/README.md)
  - single API response processing per call type (timer: `music.data.raw.spotify.api.response.process`, tags: `api_call_type`, `status`)
- [spotify-staging-applicator](../music/data/raw/spotify/etl/spotify-staging-applicator/README.md)
  - single staging iteration application (timer: `music.data.raw.spotify.staging.apply`, tag: `status`)

**Rationale for @Timed vs @Observed**:
- **Generation/Processing**: Use `@Timed` because these are async batch operations without shared trace context (database is the only connector between execution and parsing phases)
- **API Call Execution**: Use `@Observed` because these are synchronous operations that form a trace hierarchy

## What is Excluded

### Scheduled Tasks

The following are **not traced** to reduce noise:

- **All `@Scheduled` methods**: Filtered by `skipScheduledTasksObservationPredicate`
- **Span name pattern**: `tasks.scheduled.execution.*`

**Note**: Individual operations *within* schedulers (like `makeApiCall()`) are still traced via explicit `@Observed` annotations.

### Actuator and Health Endpoints

HTTP requests to actuator and health endpoints are **not traced**:

- **Patterns**: `/actuator/**`, `/health`'
- **Endpoints excluded**:
  - `/actuator/health`
  - `/actuator/prometheus`
  - `/actuator/metrics`
  - `/health`
- **Rationale**: Prometheus scrapes metrics every 15s; tracing these creates massive noise (8,640 traces/day per service)
- **Configuration**: `SharedObservabilityConfiguration.skipActuatorEndpointsObservationPredicate()`

## Sampling Configuration

- production, testing: 10%
- development: 100%

## Storage

Currently all traces are volatile. It is planned to introduce a persistent storage for Zipkin in the future.

## See Also

- [SERVICES.md](SERVICES.md): Service ports and deployment configuration
