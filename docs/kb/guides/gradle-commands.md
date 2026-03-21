# Art Universe - Modules Reference

> **For complete module listings with types, purposes, and documentation links, see [Modules Reference](../../MODULES.md).**

## Quick Commands

> **Note**: All Gradle commands must be executed from the project root directory.

```bash
# Build specific module
./gradlew :<module-path>:build

# Test specific module
./gradlew :<module-path>:test

# Build module without tests
./gradlew :<module-path>:build -x test

# Run integration tests only
./gradlew :<module-path>:integrationTest
```

**Example:**
```bash
./gradlew :music:data:master:build
./gradlew :music:data:master:test
```

## Build Patterns

### Build All Modules
```bash
./gradlew build -x test          # Build without tests
./gradlew build                  # Build with tests
./gradlew test                   # Run all tests
./gradlew integrationTest        # Run integration tests only
```

### Build by Category
```bash
# All common modules
./gradlew :common:build

# All LastFM modules
./gradlew :music:data:raw:lastfm:build

# All music modules (master + quiz + ui)
./gradlew :music:build
```

### Test Patterns
```bash
# Run tests excluding integration tests
./gradlew test -PexcludeIntegrationTests

# Run tests for specific module
./gradlew :music:data:master:test

# Run integration tests for specific module
./gradlew :music:data:master:integrationTest
```

### Code Coverage

```bash
# Print classes below 80% line coverage for a module
./gradlew :<module-path>:jacocoSummary

# Example — single module
./gradlew :music:data:raw:spotify:spotify-rest-api:jacocoSummary

# Example — multiple modules at once
./gradlew :music:data:raw:spotify:etl:spotify-calls-generator:jacocoSummary \
          :music:data:raw:spotify:etl:spotify-calls-performer:jacocoSummary
```

`jacocoSummary` runs tests, generates the JaCoCo XML report, then prints only classes below **80% line coverage** in the format:

```
com/example/SomeClass -> 62% (missed: 15)
```

If the output is empty, all classes already meet the 80% threshold.

The raw JaCoCo XML report is at: `<module>/build/reports/jacoco/test/jacocoTestReport.xml`

For a structured analysis-and-improve workflow, use the `/analyze-coverage` skill.

### Docker Images
```bash
# Build all Docker images (via convenience script)
./scripts/build-images.sh

# Build all Docker images (via Gradle directly)
./gradlew dockerBuildAll -x test

# Build a single module's image
./gradlew :music:quiz:dockerBuild

# List available docker tasks
./gradlew tasks --group=docker
```

Modules opt in to Docker image building by declaring `ext.dockerImageName` in their `build.gradle`. The `dockerBuild` task is automatically registered by the convention plugins (`spring-boot-app`, `react-ui`) or defined inline (liquibase service).

## See Also

- [Project Modules Index.md](../../MODULES.md) - All services with ports and deployment info
- [Services Reference](../../SERVICES.md) - All services with ports and deployment info
- [Development Reference](../../DEVELOPMENT.md) - Development workflow and environment setup
- [Architecture Reference](../../ARCHITECTURE.md) - System architecture and design patterns
