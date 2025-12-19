# LastFM Tasks Coordinator

**IMPORTANT!** After extraction from Lastfm monolith the module stopped serving its coordination purpose.
To be a coordination point for disconnected modules it has to be redesigned
The code was kept and the module was not excluded from dependencies to make the future update seamless.

## Overview

LastFM Tasks Coordinator is a shared library module that coordinates database-related tasks across the LastFM subsystem.
It prevents database operations from running during maintenance by implementing a task queue and rejecting tasks during maintenance.

Maintenance is triggered by [ETL REST API module](../lastfm-etl-rest-api/README.md) in two ways:
- scheduled daily execution
- manual (from UI via REST endpoint)


## Key Components

- [TaskCoordinator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/maintenance/service/TaskCoordinator.java) - Central coordinator using state machine to prevent tasks from running during maintenance
- [DbTaskQueue.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/maintenance/service/DbTaskQueue.java) - Thread-safe FIFO queue with duplicate detection
- [TaskCoordinationConfig.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/config/TaskCoordinationConfig.java) - Spring Boot auto-configuration providing 5-thread executor pool. The number is calculated based on max possible number of **different** tasks running in parallel.


## Usage Patterns

### State Machine

[TaskCoordinator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/maintenance/service/TaskCoordinator.java) uses a three-state [machine](../../../../../../docs/kb/patterns/backend/state-machine.md):

- **NORMAL**: Tasks execute normally via executeIfAllowed()
- **REQUESTED**: Maintenance requested, new tasks blocked, waiting for running tasks to complete
- **RUNNING**: Maintenance tasks executing, all regular tasks blocked

State transitions: NORMAL → REQUESTED (when maintenance requested) → RUNNING (when all tasks finish) → NORMAL (when maintenance completes)

### Regular Task Execution

ETL modules should wrap database operations with executeIfAllowed() to respect maintenance windows:

- Inject [TaskCoordinator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/maintenance/service/TaskCoordinator.java) via Spring autowiring
- Call executeIfAllowed(task, taskKey) where taskKey uniquely identifies the task
- Tasks with duplicate keys are silently skipped if already running
- Tasks are silently skipped when status is REQUESTED or RUNNING
- Otherwise, tasks are submitted to the 5-thread executor pool for execution

### Maintenance Execution

Maintenance operations should be triggered via requestMaintenance():

- Call requestMaintenance(maintenanceTask) to schedule maintenance
- Throws IllegalStateException if maintenance is already requested or running
- Coordinator blocks new tasks, waits for running tasks to complete, then executes maintenance
- After maintenance completes, coordinator returns to NORMAL state and regular tasks resume

### Task Deduplication

The coordinator prevents duplicate task execution:

- Each task requires a unique taskKey parameter
- If a task with the same key is already running, subsequent calls are ignored
- Keys are automatically removed when tasks complete

### Thread Safety

The coordinator uses synchronized blocks for state transitions and atomic counters for tracking running tasks.
The [DbTaskQueue](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/maintenance/service/DbTaskQueue.java) has all methods synchronized for thread-safe access. 
Regular tasks execute on a multi-thread pool while maintenance tasks execute sequentially on a single-thread executor.


## Adding to Your Module

Add dependency to your module's build.gradle and autowire [TaskCoordinator](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/maintenance/service/TaskCoordinator.java). The coordinator and executor beans are auto-configured via [TaskCoordinationConfig](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/config/TaskCoordinationConfig.java). No environment variables or application properties are required.


## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - Internal state machine (NORMAL → REQUESTED → RUNNING) coordinates maintenance windows


## Related Documentation

- [LastFM Modules Overview](../../README.md) - Overview of all LastFM modules
- [Project Modules Index](../../../../../../docs/MODULES.md) - All modules index
