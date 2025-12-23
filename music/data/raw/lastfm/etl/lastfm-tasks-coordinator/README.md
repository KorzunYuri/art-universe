# LastFM Tasks Coordinator

## Overview

Distributed task coordination library for the LastFM subsystem.
Enables multiple application instances to coordinate task execution and maintenance operations using database-backed distributed state or in-memory state for testing.

## Key Components

### Core Services

- [TaskCoordinator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/TaskCoordinator.java) - Main coordinator service using state machine to prevent concurrent task execution and manage maintenance windows
- [CoordinationStateProvider.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/coordination/CoordinationStateProvider.java) - Provider interface for pluggable coordination backends
- [DbTaskQueue.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/DbTaskQueue.java) - Thread-safe FIFO queue with duplicate detection

### Coordination Providers

- [DatabaseCoordinationProvider.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/coordination/provider/DatabaseCoordinationProvider.java) - **Default** production provider. Uses database tables for distributed coordination with heartbeat-based crash detection and automatic cleanup of stale tasks
- [InMemoryCoordinationProvider.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/coordination/provider/InMemoryCoordinationProvider.java) - Single-instance provider for testing. Fast, no database overhead

### Configuration

- [TaskCoordinationAutoConfiguration.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/TaskCoordinationAutoConfiguration.java) - Runtime auto-configuration with database provider by default
- [TaskCoordinationTestAutoConfiguration.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/TaskCoordinationTestAutoConfiguration.java) - Test auto-configuration with in-memory provider
- [CoordinationProperties.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/CoordinationProperties.java) - Configuration properties with programmatic defaults
- [application.yml](src/main/resources/application.yml) - Configuration documentation

## Usage Patterns

### State Machine

[TaskCoordinator](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/TaskCoordinator.java) uses a three-state machine:

- **NORMAL**: Tasks execute normally
- **REQUESTED**: Maintenance requested, new tasks blocked, waiting for running tasks to complete
- **RUNNING**: Maintenance in progress, all tasks blocked

Transitions: `NORMAL` → `REQUESTED` → `RUNNING` → `NORMAL`

### Regular Task Execution

Wrap database operations with `coordinator.executeIfAllowed(task, taskKey)`:
- Tasks are silently skipped if maintenance is active (REQUESTED or RUNNING)
- Tasks with duplicate keys are silently skipped if already running
- Otherwise, tasks execute on the thread pool

### Maintenance Execution

Trigger maintenance via `coordinator.requestMaintenance(maintenanceTask)`:
- Transitions to REQUESTED state
- Waits for running tasks to complete
- Executes maintenance task
- Returns to NORMAL state

Throws `IllegalStateException` if maintenance is already requested or running.

### Configuration

**Defaults** (no configuration required):
- Provider: `database`
- Heartbeat interval: 30 seconds
- Stale timeout: 120 seconds
- Cleanup interval: 60 seconds

Override via environment variables: `LASTFM_COORDINATION_PROVIDER`, `LASTFM_COORDINATION_HEARTBEAT_INTERVAL_SECONDS`, etc. See [application.yml](src/main/resources/application.yml) for full list.

### Thread Safety

- [DatabaseCoordinationProvider](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/coordination/provider/DatabaseCoordinationProvider.java) - Thread-safe via database transactions
- [InMemoryCoordinationProvider](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/coordination/provider/InMemoryCoordinationProvider.java) - Thread-safe via concurrent data structures
- [TaskCoordinator](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/TaskCoordinator.java) - Synchronized state transitions
- [DbTaskQueue](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/DbTaskQueue.java) - All methods synchronized

## Adding to Your Module

1. Add dependency: `implementation project(':music:data:raw:lastfm:etl:lastfm-tasks-coordinator')`
2. Inject `TaskCoordinator` via constructor injection
3. Auto-configuration provides all beans automatically

### Testing

**Integration tests** (`@SpringBootTest`): [TaskCoordinator](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/TaskCoordinator.java) auto-configured with in-memory provider

**Slice tests** (`@DataJpaTest`, `@WebMvcTest`): Must explicitly import [TaskCoordinationTestAutoConfiguration](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/TaskCoordinationTestAutoConfiguration.java) via `@Import`

## Database Schema

Coordination tables in `mu_raw_lastfm` schema:
- `coordination_state` - Global coordination state
- `coordination_instance` - Instance registry with heartbeats
- `coordination_running_task` - Currently executing tasks

Migrations: [Coordination State Changeset](../../migrations/lastfm-liquibase-resources/src/main/resources/db/migration/muraw/liquibase/changesets/0035-coordination-state/)

## Patterns Used

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - Three-state coordination
- Strategy Pattern - Pluggable [CoordinationStateProvider](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/coordination/CoordinationStateProvider.java) implementations

## Related Documentation

- [LastFM Modules Overview](../../README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)
