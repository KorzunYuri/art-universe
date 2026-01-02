# Patterns Index

This directory contains reusable implementation patterns (SOPs - Standard Operating Procedures) for the Art Universe project.

---

## What Are Patterns?

**Patterns** are reusable implementation standards that describe *how* we build things. They are:
- Technology-specific (Backend vs Frontend)
- Extracted from common practices
- Applied across multiple modules
- Documented with examples from the codebase

---

## Available Pattern Categories

### Backend Patterns
**Path**: [backend/](backend/overview)

Implementation patterns for Spring Boot backend development.

**Categories**:
- **Entities** ([backend/entities/](backend/entities/overview.md)) - JPA entity design patterns
- **API** ([backend/api/](backend/api/overview.md)) - REST API patterns
- **Configuration** ([backend/configuration/](backend/configuration/environment-profiles.md)) - Spring Boot configuration
- **Database** ([backend/database/](backend/database/overview.md)) - Schema and migration patterns
- **Testing** ([backend/testing/](backend/testing/overview.md)) - Backend testing patterns

**See**: [Backend Patterns](backend/overview)


## Pattern Structure

### Two-Tier Organization

Each category with 2+ aspects uses a two-tier structure:

**Tier 1: Overview/Index**
- File: `overview.md`
- Purpose: Quick reference for all patterns in category
- Content: Pattern summaries, when to use, quick examples

**Tier 2: Deep-Dive**
- Files: Individual pattern files (e.g., `coded-enums.md`)
- Purpose: Complete implementation guide
- Content: Purpose, steps, examples, testing, troubleshooting

**Example Navigation**:
```
backend/entities/overview.md          ← Start here (index)
    ↓
backend/entities/coded-enums.md      ← Deep dive
```

#### Single-aspect categories

Categories with only one aspect don't the `overview.md` - the single doc can be referenced to in the parent `overview.md` or 

---

## How to Use Patterns

### Finding a Pattern

**Scenario**: "I need to create an entity with a status field"

**Navigation**:
1. Go to [backend patterns](backend/overview)
2. Find [entities category](backend/entities/overview.md)
3. Read [Entity Patterns Overview](backend/entities/overview.md)
4. Find "Coded Enum Pattern"
5. Deep dive: [Coded Enum Pattern](backend/entities/coded-enums.md)

### Applying a Pattern

1. **Read the purpose**: Understand what problem it solves
2. **Check "When to Use"**: Confirm it fits your scenario
3. **Follow implementation steps**: Apply step-by-step
4. **Review examples**: See how it's used in codebase
5. **Test your implementation**: Follow testing guidelines

### Creating a New Pattern

When you identify a reusable pattern:

1. **Extract from implementation**: Find common approach used 3+ times
2. **Choose category**: Backend vs Frontend, then subcategory
3. **Document pattern**:
   - Purpose and when to use
   - Implementation steps
   - Code examples from codebase
   - Testing approach
   - Related patterns
4. **Update category overview**: Add to index
5. **Reference from features/modules**: Link from relevant docs

---

## Quick Reference

### When to Use Which Category

| Task                       | Category | Example Pattern |
|----------------------------|----------|-----------------|
| Create JPA entity          | Backend > [Entities](backend/entities/overview.md) | [BaseEntity](backend/entities/base-entity.md), [Coded Enums](backend/entities/coded-enums.md) |
| Create REST API endpoint   | Backend > [API](backend/api/overview.md) | [REST Conventions](backend/api/conventions.md), [Lookup Pattern](backend/api/lookup.md) |
| Create database migration  | Backend > [Database](backend/database/overview.md) | [Liquibase](backend/database/liquibase.md) |
| Track attribute history    | Backend > [Database](backend/database/overview.md) | [SCD2 Attribute History](backend/database/scd2-attribute-history.md) |
| Test JPA repositories      | Backend > [Testing](backend/testing/overview.md) | [Testing with Persistence Layer](backend/testing/testing-with-persistence-layer.md) |
| Test REST controllers      | Backend > [Testing](backend/testing/overview.md) | [Testing Controllers](backend/testing/testing-controllers.md) |
| Configure environments     | Backend > Configuration | [Environment Profiles](backend/configuration/environment-profiles.md) |
| Share config from library  | Backend > Configuration | [Spring Config Import](backend/configuration/spring-config-import.md) |
| Manage entity state        | Backend > Core | [State Machine](backend/state-machine.md) |
| Create plugin architecture | Backend > Core | [Strategy Registry](backend/strategy-registry.md) |
| Organize project structure | Backend > Core | [Project Structure](backend/project-structure.md) |

---

## Documentation Standards

For guidelines on creating and maintaining pattern documentation, see:
- **[Pattern Documentation Guide](DOCUMENTATION_GUIDE.md)** - Standards for pattern documentation including the 4-Question Litmus Test and code example policy

---

## See Also

- [**Modules**](../../MODULES.md): Modules implementing these patterns
- [**Guides**](../guides/README.md): Project-wide guides
- [**Documentation Structure Overview**](../README.md): Documentation structure overview
