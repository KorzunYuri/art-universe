# State Machine Pattern

## Purpose

Manage entity lifecycle through explicit state transitions with validation to ensure data integrity and coordinate operations across modules.

## When to Use

- Entity lifecycle management (API calls, responses, approvals, pipeline executions)
- Cross-module coordination (ETL pipeline stages, maintenance windows)
- Status tracking with business rules about valid transitions
- Preventing invalid state changes

## Implementation Approaches

### Approach 1: Coded Enum with Transition Validation

For entity status fields stored in database. Combines [Coded Enum pattern](entities/coded-enums.md) with TransitionAware interface.

**Key components**:
- Enum implements `Coded` and `TransitionAware<T>`
- Inner `Transition` enum defines all valid state transitions
- `isValidTransition(target)` validates transitions using transition map
- Stored as INTEGER in database via coded enum converter

**Example**: [ApiCallStatus.java](../../../common/data/raw/data-raw-commons-jpa/src/main/java/yurykorzun/art/universe/common/data/raw/api/client/entity/ApiCallStatus.java)

States: `CREATED` → `PENDING` → `PROCESSING` → `SUCCESSFUL`/`FAILED`/`DUE_TO_RETRY`

Valid transitions defined in inner Transition enum with EnumMap for O(1) lookup.

### Approach 2: Private Enum for Internal State

For runtime state management not persisted to database.

**Key components**:
- Private enum within class (e.g., `private enum Status { NORMAL, REQUESTED, RUNNING }`)
- State transitions managed through synchronized methods
- No database persistence required
- Simpler than coded enums when no DB storage needed

**Example**: [TaskCoordinator.java](../../../music/data/raw/lastfm/etl/lastfm-tasks-coordinator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/TaskCoordinator.java)

States: `NORMAL` → `REQUESTED` → `RUNNING` → `NORMAL`

State transitions synchronized to prevent race conditions.


## Key Principles

**Explicit transitions**: Define all valid state transitions explicitly rather than allowing arbitrary changes

**Validation before transition**: Use `isValidTransition()` or synchronized checks to prevent invalid state changes

**Terminal states**: Identify final states (e.g., SUCCESSFUL, FAILED) where no further transitions occur

**Cross-module coordination**: Downstream modules read upstream entity status to determine what to process (e.g., Response Parser reads API Calls with COMPLETED status)

**Thread safety**: For runtime state machines, use synchronization or atomic operations to prevent race conditions

## Related Patterns

- [Coded Enums](entities/coded-enums.md) - Foundation for database-persisted state machines
- [Base Entity](entities/base-entity.md) - Entities using state machine pattern
- [SCD2 Attribute History](database/scd2-attribute-history.md) - Track state changes over time
