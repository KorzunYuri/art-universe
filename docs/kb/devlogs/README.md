# Development Log (Devlog)

**Purpose**: Chronological record of AI-assisted development sessions, capturing decisions, rationale, and the patterns/rules they produce.

---

## About Devlogs

### What Gets Documented

Each development session that produces:
- **New features** or significant enhancements
- **Architectural decisions** that create patterns or rules
- **Refactorings** that establish new practices
- **Bug fixes** that lead to rule changes

### What Doesn't Get Documented

- Trivial changes (typo fixes, formatting)
- Purely conversational exchanges
- Repeated application of established patterns without new decisions

---

## Task Documentation Template

Each task is documented in a folder: `tasks/YYYY-MM-DD_{short-description}/`

### File Structure

Every task contains exactly three files:

1. **conversation.md** - Decision-focused conversation flow
2. **requirements.md** - Final requirements in polished form
3. **implementation.md** - Patterns, classes, decisions, cross-references

## Bidirectional Linking

### Decision Trail

When a devlog documents a decision that creates a pattern/rule:

**In the devlog** (`implementation.md`):
```markdown
## Decision Trail

### Decision: Use Coded Enum for Pipeline Status

**Pattern created**: [Coded Enum Pattern](../patterns/backend/entities/coded-enums.md)

**Rationale**: Need integer storage with type safety...
```

**In the pattern** (e.g., `patterns/backend/entities/coded-enums.md`):
```markdown
## Origin

**Created in**: [Pipeline Metadata Persistence Task](../../devlogs/tasks/2025-12-04_pipeline-metadata-persistence/README.md)
**Used in**: {list of modules, metafeatures, with references to their docs}

**Original Decision**: Initially decided for Pipeline execution status,
later generalized to all enum types.
```

This creates a **decision trail** showing WHY patterns exist.

---

## Task List

Tasks are listed in **reverse chronological order** (newest first).

*No tasks documented yet. Use `/update-devlog` to create your first devlog entry.*

---

## Usage Guide

### Creating a New Devlog Entry

Use the `/update-devlog` slash command after completing a task:

```bash
/update-devlog
```

The command will:
1. Read DEVLOG_UPDATE_GUIDE.md
2. Analyze current session
3. Propose task structure (may suggest splitting into multiple tasks)
4. Get your approval
5. Create/update devlog entry
6. Update cross-references in patterns/features

---

## Documentation Standards

For guidelines on creating and updating devlog entries, see:
- **[Devlog Update Guide](DEVLOG_UPDATE_GUIDE.md)** - Complete guide for creating and structuring devlog entries

---

## See Also

- [**Patterns**](../patterns/README.md): Patterns with origin links to devlogs
- [**Modules**](../../MODULES.md): Modules documented in devlogs
- [**Documentation Structure Overview**](../README.md): Documentation structure overview
