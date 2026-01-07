# commons-observability

The module provides monitoring and observability configuration for all Spring Boot applications in the project.

## Key Components

### Actuator Configuration

- [Main Configuration](src/main/resources/commons-observability.yml)
  - Exposes health and prometheus endpoints via Spring Boot Actuator
  - Configures prometheus endpoint as read-only
  - Disables JMX to reduce overhead
  - Works with both **Spring Web MVC** and **Spring WebFlux** (Reactive)
- [Development Overrides](src/main/resources/commons-observability-dev.yml)
  - Exposes all actuator endpoints for development
  - Enables JMX for advanced monitoring and debugging
  - Activated automatically when `dev` profile is active

### Trace Noise Reduction

[SharedObservabilityConfiguration](src/main/java/yurykorzun/art/universe/common/observability/config/SharedObservabilityConfiguration.java) provides default rules for observations:
- Removes scheduled tasks from tracing. 
  - To change the default behaviour, consumer module must provide `ObservationFilter` bean with `SharedObservabilityConfiguration.SCHEDULED_OBSERVATION_FILTER_BEAN_NAME` as the name
- Removes actuator endpoints from tracing.
  - to change the default behaviour, consumer module must provide `ObservationFilter` bean with `SharedObservabilityConfiguration.ACTUATOR_OBSERVATION_FILTER_BEAN_NAME` as the name

## Web Framework Compatibility

This module is **fully compatible** with both:
- **Spring Web MVC** (servlet-based): Traditional blocking web applications
- **Spring WebFlux** (reactive): Non-blocking reactive web applications

Spring Boot Actuator auto-detects which web framework is on the classpath and configures itself accordingly. No additional configuration is needed.

## Usage

### Adding to a Module

- Add `:common:commons-observability` dependency to module's `build.gradle`
- Add Spring Web MVC (`org.springframework.boot:spring-boot-starter-web`) or Spring WebFlux (`org.springframework.boot:spring-boot-starter-webflux`) dependency to module's `build.gradle`
- Import configurations:
```yaml
config:
  import:
    - classpath:commons-observability.yml
    - optional:classpath:commons-observability-${spring.profiles.active}.yml 
```

### Port Configuration

**Web applications** (REST APIs):
- Actuator endpoints are exposed on the **same port** as the main application
- Example: If your app runs on port `8080`, actuator is at `http://localhost:8080/actuator`
- To override ports, modify the `application[-[profile]].yml` of the consumer module:
  - To change the port for the application AND actuator, override `server.port` property
  - To expose the actuator on a **separate** port, set `management.server.port` property

### Available Endpoints

When this module is on the classpath, the following endpoints are automatically available:

- **Health**: `/actuator/health` - Application health status
- **Prometheus**: `/actuator/prometheus` - Prometheus-formatted metrics

In development (`dev` profile), all actuator endpoints are exposed.

## Monitoring Integration

This module is designed to work with:
- **Prometheus** - Metrics collection and time-series storage
- **Grafana** - Metrics visualization and dashboards

See [SERVICES.md](../../docs/SERVICES.md) for Prometheus scrape configuration.

## See Also

- [Architecture - Monitoring and Observability](../../docs/ARCHITECTURE.md#monitoring-and-observability)
- [Services Reference](../../docs/SERVICES.md#actuator-endpoints)
