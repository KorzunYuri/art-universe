# Spring Config Import Pattern

## Purpose

Explicitly import shared Spring Boot configuration from library modules to consuming application modules.
This pattern ensures predictable, profile-aware configuration sharing.

**Problem it solves:**
When library modules (like `commons-observability`) provide configuration via `application.yml` files, Spring Boot doesn't merge these with consuming modules' own `application-{profile}.yml` files. This leads to missing or incomplete configuration.

**Benefits:**
- **Explicit**: Clear declaration of configuration dependencies
- **Predictable**: No reliance on automatic merging behavior
- **Profile-aware**: Supports environment-specific overrides (dev, local, prod)
- **Maintainable**: Easy to trace where configuration comes from

## When to Use

Use the Spring Config Import pattern when:

- **Creating a library module** that provides Spring Boot configuration properties (not just Java beans)
- **Sharing configuration** like actuator endpoints, security settings, or other Spring Boot properties across multiple modules
- **Need profile-specific overrides** for different environments (dev, local, prod)
- **Configuration conflicts occur** when relying on automatic classpath scanning

Do NOT use when:

- **Library only provides Java beans/components** - Use `@ComponentScan` and `@Import` instead
- **Configuration is module-specific** - Keep it in the module's own `application.yml`
- **Simple property values** - Use `@Value` or `@ConfigurationProperties` for injecting values

## Implementation Steps

### Step 1: Prepare Library Module

Create named configuration files in your library module:

**File naming convention:**
- Base config: `{module-name}.yml` (e.g., `commons-observability.yml`)
- Profile-specific: `{module-name}-{profile}.yml` (e.g., `commons-observability-dev.yml`)

**Location**: `src/main/resources/`

**Example**: See `commons-observability` module structure:
- [commons-observability.yml](../../../../../common/commons-observability/src/main/resources/commons-observability.yml) - Base configuration
- [commons-observability-dev.yml](../../../../../common/commons-observability/src/main/resources/commons-observability-dev.yml) - Development profile overrides

### Step 2: Configure Consuming Module

Add `spring.config.import` directive to consuming module's `application.yml`:

```yaml
spring:
  config:
    import:
      - classpath:commons-observability.yml
      - optional:classpath:commons-observability-${spring.profiles.active}.yml
```

**Key Points:**
- Import both base and profile-specific configs
- Use `${spring.profiles.active}` placeholder for dynamic profile resolution to load profile-specific properties automatically
- Order matters: later imports can override earlier ones

### Step 3: Document in Library README

Update the library module's README to document the import pattern:

**Example**: [commons-observability README](../../../../../common/commons-observability/README.md)

## See Also

- [Environment Profiles Pattern](environment-profiles.md) - Profile configuration and conventions
- [commons-observability Module](../../../../common/commons-observability/README.md) - Reference implementation
- [Spring Boot Config Import Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files.importing) - Official Spring Boot docs
