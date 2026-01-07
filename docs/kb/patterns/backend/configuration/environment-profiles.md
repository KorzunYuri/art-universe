# Environment Profiles Configuration

## Purpose

Documents Art Universe's Spring Boot profile configuration for dev, local, and prod environments.

## When to Use

Use this pattern when:
- Setting up a new module in the Art Universe system
- Configuring environment-specific settings (database, ports, logging)
- Understanding how Art Universe separates dev/local/prod configurations

## Profile Strategy

Art Universe uses the following profiles mirroring the envs/purposes:

| Profile  | Purpose                            | Database                | Port Range | Logging Level |
|----------|------------------------------------|-------------------------|------------|---------------|
| `dev`    | Development (database dev)         | PostgreSQL dev instance | 7xxx       | DEBUG         |
| `local`  | Local development (Docker)         | PostgreSQL local/H2     | 9xxx       | DEBUG         |
| `prod`   | Production                         | PostgreSQL prod         | 8xxx       | INFO/WARN     |

There is also a `common` profile that goes with [Common Module](../../../../../common/commons-context/README.md) module and adds a shared functionality and beans to all backend modules.



## Service Ports by Profile

Art Universe uses consistent port conventions across all services, for example:

| Service | Dev | Local | Production |
|---------|-----|-------|------------|
| **Music Data** | :7082 | :9082 | :8082 |
| **Music Quiz** | :7083 | :9083 | :8083 |
| **LastFM REST API** | :7081 | :9084 | :8081 |


## Testing Profile

Art Universe uses a separate `test` profile for integration tests. 

Note that it doesn't override the database connection string as it is formed using env variables.
See [Testing With Persistence Layer](../testing/testing-with-persistence-layer.md) for details.

**Usage in Tests**:

See implementation in test files across modules:
```java
@SpringBootTest
@ActiveProfiles("test")
class ArtistServiceTest {
    // Test uses application-test.yml
}
```

## Best Practices

1. **Never hardcode environment-specific values** - always use profiles
2. **Use consistent port ranges** - 7xxx for dev, 9xxx for local, 8xxx for prod
3. **Test profile should be closest to prod** - minimize differences
4. **Log SQL only in dev/test** - never in production (performance impact)
5. **Use WARN in prod** - minimize logging overhead


## See Also

- [Module: Music Data Master](../../modules/mu-data-master/README.md)
- [Module: Music Quiz](../../modules/mu-quiz/README.md)
- [Database Patterns](../database/liquibase-workflow.md)
