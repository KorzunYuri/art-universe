# LLM-Optimized Knowledge Base

This is the root of a LLM-optimized documentation hub for Art Universe project.

For human-oriented documentation start from the [Core README](../../README.md). The knowledge hub intersects with them but is more detailed and granular.

## Documentation Dimensions

- [Modules](../MODULES.md) - Module-specific contexts for focused work. References features and patterns used. Use when deep-diving into a single module.
- [Features](features/README.md) - Module-specific contexts for focused work. References features and patterns used. Use when deep-diving into a single module.
- [Patterns](patterns/README.md) - Reusable implementation patterns (SOPs) with two-tier structure: index (overview.md) + deep-dives. Use when applying standard patterns to new code.
- [Guides](guides/README.md) - Project-wide guides and meta-knowledge: architecture overview, workflows, troubleshooting. Use when onboarding or understanding high-level system design.
- [Devlogs](devlogs/README.md) - Development history and decision trails. Documents decisions that produced patterns/rules/features with bidirectional links (devlog ↔ pattern origin).

### Pattern vs Feature vs Module

- **Pattern** is *How* we build. Example: [coded-enums.md](backend/entities/coded-enums.md)
- **Metafeature** is *What* we build. Example: [entity-binding/](../metafeatures/entity-binding/README.md)
- **Module** is *Where* code lives. Example: [mu-data-master/](../modules/mu-data-master/README.md)

**Relationships**:
- **Metafeatures use Patterns**: Entity Binding uses Coded Enum pattern
- **Modules use Patterns**: mu-data-master uses Entity patterns
- **Patterns are extracted from Modules**: Common code → Pattern


## Cross-Reference Format

Documentation uses markdown links for all cross-references with descriptive text.

**Examples**:
- `[Entity Binding - Backend](../../metafeatures/entity-binding/backend.md)` - Reference to metafeature document
- `[Coded Enum Pattern](../entities/coded-enums.md)` - Reference to pattern
- `[Music Data Master Module](../../modules/mu-data-master/README.md)` - Reference to module
- `[Troubleshooting Guide](../../guides/troubleshooting.md)` - Reference to guide
- `[Pipeline Metadata Task](../../devlogs/tasks/2025-12-05_pipeline-metadata/README.md)` - Reference to devlog

**Link Text Guidelines**:
- Use descriptive text (not just filenames)
- Capitalize like titles
- Include document type when helpful (Pattern, Metafeature, Module)


## Key Design Principles

### 1. Single Source of Truth.
- Each concept is documented once, referenced everywhere else.

### 2. Two-way references
- Whenever a section in document A references document/section B, there must be a reference from document/section B to the referencing section in A.
- Whenever changes in the project remove/create such a reference, it must be documented.
This helps to keep the documentation up-to-date and make it more navigable.

### 3. Index
Each README.md must contain Index section listing all sub-documents/directories with their brief descriptions, allowing LLM to decide if their content is needed in the context of current task.

This ensures:
- No orphaned documentation
- Clear navigation paths from any entry point
- LLMs can discover all documentation by following links from kb/README.md

### 4. LLM-Optimized Context Loading
- Index files provide quick navigation
- N-tier pattern structure (overview -> deep-dive -> more granular aspects -> ...)

### 5. Maintainability
- Small, focused files
- Minimal duplication through cross-referencing
- Clear separation of concerns

### 6. Navigability

**All documentation must be accessible via references starting from kb/README.md.**


## Other resources

For the details of project architecture & configuration, visit:
- [Architecture reference](../../docs/ARCHITECTURE.md): System architecture overview
- [Modules reference](../MODULES.md): Complete list of modules
- [Services reference](../../docs/SERVICES.md): List of deployable services with ports
