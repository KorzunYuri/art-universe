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

### Approach 2: Coded Enum as a State Machine

For state management.

**Key components**:
- Coded enum defines states (e.g., `NORMAL`, `REQUESTED`, `RUNNING`)
- Provider interface abstracts state management
- State transitions managed through provider implementations


## Key Principles

**Explicit transitions**: Define all valid state transitions explicitly rather than allowing arbitrary changes

**Validation before transition**: Use `isValidTransition()` or synchronized checks to prevent invalid state changes

**Terminal states**: Identify final states (e.g., SUCCESSFUL, FAILED) where no further transitions occur

**Cross-module coordination**: Downstream modules read upstream entity status to determine what to process (e.g., Response Parser reads API Calls with COMPLETED status)

**Thread safety**: For runtime state machines, use synchronization or atomic operations to prevent race conditions

## Related Patterns

- [Coded Enums](entities/coded-enums.md) - Foundation for database-persisted state machines
- [Base Entity](entities/base-entity.md) - Entities using state machine pattern
- [Strategy Registry](strategy-registry.md) - Registry pattern for strategy components (different from enum registry)
- [SCD2 Attribute History](database/scd2-attribute-history.md) - Track state changes over time
