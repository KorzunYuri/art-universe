# commons-config-client

Spring Boot auto-configuration library that connects a consumer module to the centralized `au-config-service`.
- discovers all `ConfigurableProperty` enum constants on the classpath on startup
- registers them with the config service (idempotent)
- populates an in-memory `ConfigPropertyHolder`
- refreshes values on a background schedule.

## Key Features

- **Zero-boilerplate activation** — set one property (`au.config.client.service-url`) to enable
- **Classpath-wide auto-discovery** — no manual wiring of individual property enums
- **In-memory holder with typed getters** — `getInt()`, `getBoolean()`, `getDecimal()`, `getString()`
- **Periodic refresh** — values updated from the service in the background without restart
- **Fail-safe refresh** — a failed refresh retains the last known values and logs a warning

## Key Components

### `ConfigurableProperty`

**Components**: 
- [ConfigurableProperty.java](src/main/java/yurykorzun/art/universe/common/config/client/ConfigurableProperty.java) - Marker interface for module-specific property enums. Each enum constant is one managed property
- [PropertyType.java](src/main/java/yurykorzun/art/universe/common/config/client/PropertyType.java)
- [PropertyConstraints.java](src/main/java/yurykorzun/art/universe/common/config/client/PropertyConstraints.java)

`PropertyType` values

| Type | Java type returned by holder | Parse rule |
|------|------------------------------|------------|
| `INTEGER` | `Integer` | `Integer.parseInt` |
| `BOOLEAN` | `Boolean` | `Boolean.parseBoolean` |
| `DECIMAL` | `BigDecimal` | `new BigDecimal(...)` |
| `STRING` | `String` | identity |


### `ConfigPropertyHolder`

Thread-safe in-memory store populated before any consumer bean's `@PostConstruct` runs. Inject and call typed getters:

```java
int delay = configPropertyHolder.getInt(MyModuleProperty.SCHEDULE_DELAY_SECS);
boolean enabled = configPropertyHolder.getBoolean(MyModuleProperty.FEATURE_ENABLED);
```

**Components**: [ConfigPropertyHolder.java](src/main/java/yurykorzun/art/universe/common/config/client/ConfigPropertyHolder.java)

### `ConfigPropertyAutoRegistrator`

Owns the `ConfigPropertyHolder`. On `@PostConstruct`:
1. Scans classpath for all `ConfigurableProperty` enums under `yurykorzun.art.universe`
2. Bulk-registers them with the config service (creates defaults for new properties, returns current values for existing ones)
3. Populates the holder
4. Starts the background refresh scheduler

Because `ConfigPropertyHolder` is returned by `ConfigPropertyAutoRegistrator.getHolder()`, Spring's bean dependency chain guarantees the holder is fully populated before any consumer's `@PostConstruct` runs.

**Components**: [ConfigPropertyAutoRegistrator.java](src/main/java/yurykorzun/art/universe/common/config/client/ConfigPropertyAutoRegistrator.java)

### `CommonsConfigClientAutoConfiguration`

Activated only when `au.config.client.service-url` is set. Registers all three beans in the correct dependency order: `ConfigServiceClient` → `ConfigPropertyAutoRegistrator` → `ConfigPropertyHolder`.

**Components**: [CommonsConfigClientAutoConfiguration.java](src/main/java/yurykorzun/art/universe/common/config/client/config/CommonsConfigClientAutoConfiguration.java)

## Usage

### 1. Add the dependency

```gradle
implementation project(':common:config:config-client')
```

### 2. Set the service URL in `application.yml`

```yaml
au:
  config:
    client:
      service-url: ${AU_CONFIG_SERVICE_URL}
      refresh-interval-seconds: 30   # optional, default 5
```

### 3. Define a property enum

```java
public enum MyModuleProperty implements ConfigurableProperty {
    SCHEDULE_DELAY_SECS("mymodule.scheduler.delay-secs", PropertyType.INTEGER, "60",
        "Scheduler delay in seconds", PropertyConstraints.ofRange(1, 3600));
    // ... standard boilerplate fields and getters
}
```

### 4. Inject and read

```java
@Component
public class MyScheduler {
    private final ConfigPropertyHolder configPropertyHolder;

    @PostConstruct
    public void start() {
        int delay = configPropertyHolder.getInt(MyModuleProperty.SCHEDULE_DELAY_SECS);
        // ...
    }
}
```

### 5. (Optional) Register Change Listeners

Register a callback to be notified when a specific property's value changes during a refresh cycle:

```java
configPropertyHolder.onChange(MyModuleProperty.SOME_PROPERTY, newValue -> {
    myBean.setSomeValue((Integer) newValue);
});
```

The consumer receives the new parsed value (typed as `Object` — cast to `Integer`, `Boolean`, `BigDecimal`, or `String` per the property's `PropertyType`). Listeners are invoked **synchronously on the refresh thread**, so keep them lightweight (setting a volatile field is ideal). A failed listener logs a warning and does not block other listeners or the refresh cycle.

This is the recommended mechanism for objects that cache config values internally (e.g. a rate limiter holding `minDelayMs` as a field) — see `AdaptiveRateLimiter` wiring in performer modules for a real example.


## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `au.config.client.service-url` | *(required)* | Base URL of the config service. Also acts as the activation switch. |
| `au.config.client.refresh-interval-seconds` | `5` | How often to refresh values from the service |
| `au.config.client.connect-timeout-ms` | `3000` | HTTP connect timeout |
| `au.config.client.read-timeout-ms` | `5000` | HTTP read timeout |

## See Also

- [config-service README](../config-service/README.md) — the service this client connects to
- [Centralized Configuration Pattern](../../../docs/kb/patterns/backend/configuration/centralized-configuration.md) — full pattern documentation including property naming conventions
