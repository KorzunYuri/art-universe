# Features Index

This directory documents cross-module features - the *what* we build in the Art Universe project.


## What Are Features?

Features are complete, user-facing capabilities that span multiple modules. They describe:
- What the system does (not how it's implemented)
- End-to-end workflows across modules
- Module interactions and data flows
- User-facing functionality

Features vs Patterns: Patterns describe *how* we build (SOP), Features describe *what* we build (capabilities).


## Documented Features

- [Binding Raw Entities to Master](binding-raw-entities-to-master.md) - Core master data management process.
- [Binding Master Entities to Quiz](quiz/binding-master-entities-to-quiz.md) - Allows master entities to participate in quiz track pack generation.
- [Quiz Pack Generation Pipeline](quiz/quiz-pack-generation-pipeline.md) - Multi-step pipeline transforming master track sets through filtering, weighting, and selection to generate quiz packs.


## Documentation Rules

When to Document a Feature:
- Spans 2+ modules
- Represents user-facing capability
- Involves complex module interactions

Feature Documentation Must Include:
1. What it does - Clear capability description
2. Workflow - End-to-end process
3. Modules involved - With links to module docs
4. Cross-references - Links to relevant patterns used

Bidirectional Linking:
- Feature docs link to module docs
- Module docs reference features they implement
- Features reference patterns they use
- Patterns reference features as examples


## See Also

- [Patterns](../patterns/README.md) - Implementation patterns used by features
- [Modules](../../MODULES.md) - Module implementations
- [Guides](../guides/README.md) - Project-wide guides
