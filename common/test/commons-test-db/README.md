# Commons Test DB

The module provides common database test utilities and containerized Postgre instance via TestContainers for integration tests that require a real database.

## Features

### 1. @DataJpaTest with containerized Postgres

**When to use**: whenever you want to perform tests involving persistent layer on a real database.

#### Key Components
  - [PostgresTestContainerHolder.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresTestContainerHolder.java) - handles Postgres container creation during test runtime.
  - Annotations for database configuration:
    - [PostgresTestContainer.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresTestContainer.java) - database properties for Postgres container creation.
    - [PostgresJpaTest.java](src/main/java/yurykorzun/art/universe/common/test/db/PostgresJpaTest.java) - Meta-annotation for Spring Boot tests with persistent layer (@DataJpaTest) and containerized database. Delegates container properties to PostgresTestContainer.java

#### How to Use

- annotate test class with @PostgresJpaTest
- provide annotation properties:
  - `databaseName`
  - `username`
  - `password`
  - `schema`
  - (optional) `initScript` - relative path to initial database script (will be executed on tests startup)
- update Spring context with datasource connection properties from the container (watch usage example below)

#### Examples of usage

- [LastfmJpaTest.java](../../../music/data/raw/lastfm/test/lastfm-commons-test-db/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/archetypes/LastfmJpaTest.java)
- other usages of @PostgresJpaTest across the project

### 2.Transaction logging for tests

- [TransactionListener.java](src/main/java/yurykorzun/art/universe/common/test/db/TransactionListener.java) - logs transaction events (before commit, after commit/rollback)
