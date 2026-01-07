# commons-context

The module provides common Spring Boot beans and application properties for the entire project.

## Key Components

- [CommonConfig.java](src/main/java/yurykorzun/art/universe/common/config/CommonConfig.java)
  - Spring Boot auto-configuration class that provides beans for common usage:
    - ObjectMapper - for consistent unified serialization across the project
- [application-common.yml](src/main/resources/application-common.yml)
  - Common Spring Boot profile for all modules. Disables several auto-configurations to reduce overhead.

## Usage

- Add `:common:commons-context` dependency to module's `build.gradle`
- Import configurations:
```yaml
config:
  import:
    - classpath:commons-context.yml
    - optional:classpath:commons-context-${spring.profiles.active}.yml 
```

## See Also

- [`:common:commons-observability`](../commons-observability/README.md) - For monitoring and observability configuration
