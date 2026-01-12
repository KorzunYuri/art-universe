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

## See Also

- [Project Modules Index.md](../../MODULES.md) - All services with ports and deployment info
- [Services Reference](../../SERVICES.md) - All services with ports and deployment info
- [Development Reference](../../DEVELOPMENT.md) - Development workflow and environment setup
- [Architecture Reference](../../ARCHITECTURE.md) - System architecture and design patterns
