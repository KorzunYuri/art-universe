# Backend Patterns Index

This directory contains reusable implementation patterns (SOPs) for backend development in the Art Universe project.

---

## Pattern Categories

### Core Patterns
**Path**: [backend/](.)

Cross-cutting patterns used across multiple layers.

**Files**:
- [state-machine.md](state-machine.md) - State machine pattern for entity lifecycle management (**COMPLETE**)
- [project-structure.md](project-structure.md) - Standard project structure (**COMPLETE**)

**Use when**: Managing entity state transitions, coordinating operations across modules

---

### Entities
**Path**: [entities/](entities/)

Patterns for JPA entity design, enum storage, and audit fields.

**Overview**: [entities/overview.md](entities/overview.md)

**Use when**: Creating or modifying JPA entities

---

### API
**Path**: [api/](api/)

Patterns for REST API design and implementation.

**Overview**: [api/overview.md](api/overview.md)

**Use when**: Creating or modifying REST APIs

---

### Database
**Path**: [database/](database/)

Patterns for database schema design and migrations.

**Overview**: [database/overview.md](database/overview.md)

**Use when**: Creating database schemas, migrations, or tracking historical data

---

### Testing

**Path**: [testing/](testing/)

Patterns for testing backend code.

**Overview**: [testing/overview.md](testing/overview.md)

**Use when**: Writing tests for backend code

---

### Configuration
**Path**: [configuration/](configuration/)

Patterns for Spring Boot configuration and environment management.

**Files**:
- [environment-profiles.md](configuration/environment-profiles.md) - Environment profiles configuration (**COMPLETE**)

**Use when**: Configuring Spring Boot applications, managing environments

---

## How to Use Backend Patterns

### Finding a Pattern

1. **Know what you need**: "I need to create an entity with a status enum"
2. **Find the category**: Entities
3. **Read the overview**: [entities/overview.md](entities/overview.md)
4. **Follow the deep-dive**: [entities/coded-enums.md](entities/coded-enums.md)

### Two-Tier Structure

Each category has:
1. **overview.md** - Index with quick reference for all patterns
2. **{pattern}.md** - Detailed implementation guide

**Example**:
- [entities/overview.md](entities/overview.md) ← Start here
  - Links to coded-enums.md, base-entity.md, etc.
- [entities/coded-enums.md](entities/coded-enums.md) ← Deep dive

---

## Pattern Format

Each detailed pattern file includes:
- **Purpose**: What problem it solves
- **When to Use**: Scenarios where pattern applies
- **Implementation Steps**: How to implement
- **Examples**: Code samples from codebase
- **Testing**: How to test the pattern
- **See Also**: Related patterns

---

## See Also

- [**Frontend Patterns**](../frontend/README.md) - Frontend patterns
- [**Guides**](../../guides/README.md) - Project-wide guides
- [**Patterns Index**](../README.md) - All patterns overview
