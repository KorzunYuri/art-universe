# Database Testing Patterns

## Overview

Patterns for integration testing with TestContainers PostgreSQL using annotation-based configuration.

**Key features**:
- Real PostgreSQL via TestContainers (not H2/embedded DB)
- Container reuse across test suite for performance
- Annotation-based configuration
- Domain-specific base classes with schema isolation
- Optional init scripts and Liquibase migrations
- Support for both JPA slice tests (@DataJpaTest) and full context tests (@SpringBootTest)


## How It Works

The pattern is fully implemented by `commons-test-db` module.
See [commons-test-db/README.md](../../../../../common/test/commons-test-db/README.md) for details. 

## Related Patterns

- [Testing Overview](overview.md) - Test naming conventions and categories
- [Controller Testing](testing-controllers.md) - MVC layer testing
- [Strategy Registry Pattern](../strategy-registry.md) - Pattern requiring full context tests for validation
