# commons-context

The module provides common Spring Boot beans and application properties for the entire project.

### Key Components

- [CommonConfig.java](src/main/java/yurykorzun/art/universe/common/config/CommonConfig.java)
  - Spring Boot auto-configuration class that provides beans for common usage:
    - ObjectMapper - for consistent unified serialization across the project
- [application-common.yml](src/main/resources/application-common.yml)
  - Common Spring Boot profile for all modules. Disables several auto-configurations and enables actuator endpoints.
