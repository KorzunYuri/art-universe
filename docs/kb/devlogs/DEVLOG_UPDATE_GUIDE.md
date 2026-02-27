# Devlog Update Guide

**For**: Claude Code (AI Assistant)
**Purpose**: Instructions for creating and updating devlog documentation

---

## When to Create a Devlog Entry

Create a devlog entry when the session produces:

✅ **Create Entry For**:
- New features (even if small)
- Bug fixes that reveal patterns or create rules
- Refactorings that establish new practices
- Architectural decisions
- Documentation of new patterns
- Any work that creates or modifies patterns/rules

❌ **Don't Create Entry For**:
- Trivial changes (typo fixes, formatting only)
- Purely exploratory sessions with no changes
- Repeated application of existing patterns without decisions
- Conversations with no implementation

---

## Task Granularity

**Golden Rule**: One feature/fix = One devlog entry

### One Task Examples

✅ **Single task**:
- "Implement pipeline metadata persistence" (multiple commits, one feature)
- "Add entity binding UI" (multiple files, one feature)
- "Fix approval workflow bug" (one bug, even if touches multiple files)

❌ **Should be split into multiple tasks**:
- Session implements both "pipeline metadata" AND "quiz scoring algorithm" (two features)
- Session fixes "binding bug" AND adds "new search endpoint" (unrelated changes)
- Session spans multiple days with different features each day

---

## Structure

```
YYYY-MM-DD_{short-description}/
├── conversation.md
├── requirements.md
├── implementation.md
```

### Folder

**Short Description** rules:
-  2-5 words, max 50 characters
- Descriptive of WHAT, not HOW

**Style**:
- Lowercase
- Hyphens (not underscores) between words
- No module prefix (goes in metadata)

**Good Examples**:
- `pipeline-metadata-persistence`
- `entity-binding-ui`
- `approval-workflow-bugfix`
- `coded-enum-pattern`
- `search-api-optimization`

**Bad Examples**:
- ❌ `music-quiz-pipeline-metadata` (has module prefix)
- ❌ `implement_new_pipeline` (underscores, "implement" is HOW)
- ❌ `fix-bug` (too vague)
- ❌ `update-types-and-hooks-for-pipeline-elements` (too long, too specific)
- ❌ `PipelineMetadata` (not lowercase)

### 

---

## Writing Process

### Phase 1: Session Analysis

After the session completes, analyze:

1. **What was accomplished?**
   - Features added
   - Bugs fixed
   - Refactorings done
   - Documentation created

2. **How many distinct tasks?**
   - If 1: Single devlog entry
   - If 2+: Propose split to user

3. **What decisions were made?**
   - New patterns created
   - Existing patterns modified
   - Architectural choices
   - Trade-offs considered

4. **What documentation was created/modified?**
   - New patterns
   - Updated features
   - New guides

---

### Phase 2: User Approval

**If single task**:
```
I'll create a devlog entry for this session:

Task: pipeline-metadata-persistence
Type: Feature
Modules: music:quiz, ui
Patterns Created: Pipeline Versioning, Soft Delete

Proceed? (yes/no)
```

**If multiple tasks**:
```
This session covered multiple distinct features. I propose splitting into:

1. Task: pipeline-metadata-persistence
   - Type: Feature

2. Task: notification-provider
   - Type: Enhancement

Proceed with this split? (yes/no/suggest alternative)
```

---

### Phase 3: File Creation

For each task, create three files:

#### 3.1 conversation.md

**Focus**: Decision points, not full transcript

**Include**:
- Context (why this task?)
- Key user requirements
- Important exploration findings
- Critical decisions with alternatives
- Outcome summary

**Exclude**:
- Routine code review
- Trivial clarifications
- Full file readings (just list what was read)
- Implementation details (those go in implementation.md)

**Compression Strategy**:
```markdown
### Phase 2: Exploration

**Files Explored**:
- `GenerationService.java` - Understand current generation flow
- `PipelineProcessor.java` - Check step execution logic
- `generation.sql` - Review database schema

**Key Findings**:
- Current system has no pipeline metadata persistence
- Step execution is stateless
- No versioning of step algorithms
```

Instead of copying full file contents or conversation, **summarize**.

**Template**:
```markdown
# Task: {Short Description}

**Date**: {YYYY-MM-DD}
**Modules**: {List of modules involved}
**Type**: {Feature | Bug Fix | Enhancement | Refactoring | Documentation}

---

## Context

{Brief description of what prompted this task}

### Related Documentation
- [Pattern Name](../../patterns/.../pattern.md) (if referenced during task)
- [Metafeature Name](../../metafeatures/.../README.md) (if referenced during task)
- [Module Name](../../modules/.../README.md) (if referenced during task)

---

## Conversation Flow

### Phase 1: Requirements Clarification

**User**: {Key user request/question}

**Assistant**: {Key response or analysis}

{Continue with important decision points}

---

### Phase 2: Exploration

**Files Explored**:
- `path/to/file1.ts` - {Why explored}
- `path/to/file2.java` - {Why explored}

**Key Findings**:
- {Important discovery 1}
- {Important discovery 2}

---

### Phase 3: Design & Implementation

**Key Decisions**:

1. **Decision**: {Decision title}
   - **Options Considered**:
     - Option A: {description}
     - Option B: {description}
   - **Chosen**: Option A
   - **Rationale**: {Why this option was chosen}
   - **Documented In**: [Pattern Name](../../patterns/.../pattern.md) or [Guide Name](../../guides/guide.md)

2. **Decision**: {Another decision}
   - {Same structure}

---

### Phase 4: Validation

**Tests Added/Modified**:
- {Test file 1}
- {Test file 2}

**Verification Steps**:
- {How the implementation was verified}

---

## Outcomes

### Files Created
- `path/to/new/file1.ts`
- `path/to/new/file2.java`

### Files Modified
- `path/to/existing/file1.ts` - {What changed}
- `path/to/existing/file2.java` - {What changed}

### Documentation Created/Updated
- [Pattern Name](../../patterns/.../pattern.md) - {New pattern documented}
- [Metafeature Name](../../metafeatures/.../README.md) - {Metafeature updated}

### Git Commits (Optional)
- `a40ca1ca` - music-UI: quiz: redesign the pipeline to persist pipeline metadata
- `08c47e33` - music-UI: quiz: update types, API and TanQuery hooks for pipeline elements

---

## Lessons Learned

- {Key lesson or insight from this task}
- {Pattern that should be reused}
- {Anti-pattern to avoid}
```

**Target**: 200-500 lines

---

#### 3.2 requirements.md

**Source**: User's original requirements + clarifications

**Process**:
1. Start with user's original request (verbatim or lightly edited)
2. Add refined requirements (broken down, clear acceptance criteria)
3. Note any changes from original to final
4. Mark completion status

**Template**:
```markdown
# Requirements: {Short Description}

**Status**: ✅ Completed | ⏳ In Progress | ❌ Failed

---

## User Requirements (Original)

{Original requirements as provided by user, verbatim or lightly edited}

---

## Refined Requirements

### Functional Requirements

1. **Requirement 1**
   - Description: {What needs to be done}
   - Acceptance Criteria:
     - [ ] Criterion 1
     - [ ] Criterion 2
   - Status: ✅ | ⏳ | ❌

2. **Requirement 2**
   - {Same structure}

---

### Non-Functional Requirements

1. **Performance**: {Any performance requirements}
2. **Compatibility**: {Backward compatibility needs}
3. **Testing**: {Testing requirements}

---

### Technical Constraints

- **Constraint 1**: {Description}
- **Constraint 2**: {Description}

---

## Out of Scope

{Items explicitly excluded from this task}

---

## Dependencies

### Prerequisite Tasks
- [Prior Task Name](../../devlogs/tasks/{YYYY-MM-DD_prior-task}/README.md) - {Why it's a dependency}

### Required Patterns
- [Pattern Name](../../patterns/.../pattern.md) - {Which pattern needs to be followed}

---

## Changes from Original Requirements

{If requirements evolved during implementation}

**Change 1**:
- **Original**: {What was originally requested}
- **Final**: {What was actually implemented}
- **Reason**: {Why it changed}
```

**Target**: 100-300 lines

---

#### 3.3 implementation.md

**This is the critical file for decision trail**

**Required Sections**:
1. **Patterns & Practices Applied**
   - NEW patterns created → Link to pattern docs
   - EXISTING patterns used → List with references

2. **Decision Trail**
   - Each major decision
   - Alternatives considered
   - Rationale for choice
   - Pattern/rule it produced (with link)

3. **Cross-References**
   - Features involved
   - Modules involved
   - Related devlog tasks

4. **Files Changed Summary**
   - Created files
   - Modified files
   - Deleted files

**Template**:
```markdown
# Implementation: {Short Description}

---

## Architecture Overview

{Brief description of the implementation approach}

```
{ASCII diagram if helpful}
```

---

## Patterns & Practices Applied

### New Patterns Created

1. **Pattern**: {Pattern name}
   - **Documented In**: [Pattern Name](../../patterns/.../pattern-name.md)
   - **Description**: {Brief description}
   - **Rationale**: {Why this pattern was created}
   - **Decision Reference**: See conversation.md Phase 3, Decision 2

2. **Pattern**: {Another pattern}

---

### Existing Patterns Used

| Pattern | Location | How Applied |
|---------|----------|-------------|
| Search and Lookup | [Search and Lookup Pattern](../../patterns/backend/api/search-and-lookup.md) | Used for artist autocomplete |
| Coded Enum | [Coded Enum Pattern](../../patterns/backend/entities/coded-enums.md) | Applied to ExecutionStatus |

---

## Key Classes/Components

### Backend Components

**New Classes**:
- `PipelineService` - {Purpose and location}
- `StepRunRepository` - {Purpose and location}

**Modified Classes**:
- `GenerationService` - {What changed and why}
- `GameController` - {What changed and why}

---

### Frontend Components

**New Components**:
- `PipelineVisualization` - {Purpose and location}
- `StepConfigurationForm` - {Purpose and location}

**Modified Components**:
- `GenerationList` - {What changed}

---

### Database Changes

**New Tables**:
- `pipeline` - {Purpose}
- `pipeline_step` - {Purpose}

**Modified Tables**:
- `game` - Added `pipeline_id` column

**Migration Files**:
- `db/changelog/2025/12/04-pipeline-metadata.xml`

---

## Decision Trail

### Decision 1: {Decision Title}

**What was decided**: {Clear statement of decision}

**Why it was decided**:
- {Reason 1}
- {Reason 2}

**Pattern/Rule it produced**: [New Pattern Name](../../patterns/.../new-pattern.md)

**Alternatives considered**:
- Alternative A: {Why not chosen}
- Alternative B: {Why not chosen}

**Impact**:
- {Where this decision affects the codebase}
- {Future implications}

---

### Decision 2: {Another Decision}
{Same structure}

---

## Cross-References

### Metafeatures Involved
- [Entity Binding Metafeature](../../metafeatures/entity-binding/README.md) - {How it relates}
- [Quiz Pipeline Metafeature](../../metafeatures/quiz-pipeline/README.md) - {How it relates}

### Modules Involved
- [Music Quiz Module](../../modules/mu-quiz/README.md) - {Primary module}
- [Music UI Module](../../modules/mu-ui/README.md) - {Secondary involvement}

### Related Tasks
- [Step Interface Extraction Task](../../devlogs/tasks/2025-12-01_step-interface-extraction/README.md) - {Predecessor task}

---

## Testing Approach

**Test Files Created/Modified**:
- `PipelineServiceTest.java` - {Coverage}
- `StepRunRepositoryTest.java` - {Coverage}

**Integration Tests**:
- {Description of integration test scenarios}

**Manual Testing**:
- {Critical manual test scenarios}

---

## Files Changed Summary

### Created ({N} files)
```
music/quiz/src/main/java/.../service/PipelineService.java
music/quiz/src/main/java/.../entity/PipelineStep.java
ui/src/quiz/components/PipelineVisualization.tsx
{...}
```

### Modified ({N} files)
```
music/quiz/src/main/java/.../service/GenerationService.java
ui/src/quiz/hooks/useGenerations.ts
{...}
```

### Deleted ({N} files)
```
music/quiz/src/main/java/.../PipelineRepositoryImpl.java
{...}
```

---

## Performance Considerations

- {Any performance implications}
- {Optimization decisions made}

---

## Security Considerations

- {Any security implications}
- {Security decisions made}

---

## Known Issues / Technical Debt

- {Issue 1 and plan to address}
- {Issue 2 and plan to address}

---

## Future Enhancements

- {Enhancement 1}
- {Enhancement 2}
```

**Target**: 200-400 lines

---

### Phase 4: Cross-Reference Updates

For each NEW pattern/rule created:

1. **In the devlog** (`implementation.md`):
   ```markdown
   ## Decision Trail

   ### Decision: Use Coded Enum for Execution Status

   **Pattern created**: [Coded Enum Pattern](../../patterns/backend/entities/coded-enums.md)
   ```

2. **In the pattern doc** (`patterns/backend/entities/coded-enums.md`):
   ```markdown
   ## Origin

   **Created in**: [Pipeline Metadata Persistence Task](../../devlogs/tasks/2025-12-04_pipeline-metadata-persistence/README.md)

   **Decision Context**: Needed integer storage for execution status...
   ```

3. **In the devlog index** (`devlogs/README.md`):
   ```markdown
   ### 2025-12-04

   - **[pipeline-metadata-persistence](...)** - ...
     - Patterns Created: Coded Enum Extension, Pipeline Versioning
   ```

**CRITICAL**: This bidirectional linking must be maintained!

---

## Decision Documentation

### What Qualifies as a "Decision"?

✅ **Document These**:
- Choosing between architectural approaches
- Creating new patterns or modifying existing ones
- Database schema design choices
- API design decisions
- Technology selection
- Error handling strategies
- Testing approaches

❌ **Don't Document These**:
- Applying existing patterns routinely
- Fixing obvious bugs
- Following established conventions
- Trivial naming choices

---

### Decision Format

```markdown
### Decision: {Clear, Specific Title}

**What was decided**: {One sentence summary}

**Why it was decided**:
- {Reason 1: Business driver}
- {Reason 2: Technical constraint}
- {Reason 3: Consistency with existing patterns}

**Alternatives considered**:
1. **Alternative A**: {Description}
   - Pros: {Why it was attractive}
   - Cons: {Why it was rejected}
2. **Alternative B**: {Description}
   - Pros: {Why it was attractive}
   - Cons: {Why it was rejected}

**Pattern/Rule it produced**: [Pattern Name](../../patterns/.../pattern-name.md)

**Impact**:
- {Where this decision affects the codebase}
- {What future work should follow this decision}
- {What constraints this creates}

**Decision Date**: 2025-12-04
```

---

## Pattern Documentation Standards

When documenting a new pattern in `implementation.md`:

**Pattern Quality Checklist**:
- [ ] Pattern is project-specific (not generic tool usage)
- [ ] Pattern references actual Art Universe codebase files
- [ ] Pattern shows consistency across 2+ usages
- [ ] Pattern explains WHY we do it this way (not just HOW)
- [ ] Pattern links back to this devlog as origin

**Code Example Guidelines**:
- [ ] Show minimal code (5-15 lines max for key concept)
- [ ] Reference source files for complete implementation
- [ ] Use tables for multiple examples with file paths
- [ ] Avoid duplicating code that might change

**Testing the "Project-Specific" Requirement**:
- ❌ If a Spring Boot developer could use this for ANY project → NOT project-specific
- ✅ If it explains Art Universe's unique approach → Project-specific

**Example - NOT Project-Specific** (DELETE):
```markdown
# How to Use @SpringBootApplication

@SpringBootApplication marks the main application class and enables:
- Component scanning
- Auto-configuration
- Configuration properties

Usage:
@SpringBootApplication
public class MyApplication { ... }
```
This is generic Spring Boot knowledge, not an Art Universe pattern.

**Example - Project-Specific** (KEEP):
```markdown
# CodedRegistry Configuration Pattern

Art Universe uses CodedRegistry to manage enum-to-integer mappings.

## Setup in Art Universe

In every module's configuration class, register all coded enums:

**Example from Music Data Master**:
`music/data/master/src/.../config/EnumRegistryConfig.java`

Key snippet:
@Configuration
public class EnumRegistryConfig {
    @Bean
    public CodedRegistry codedRegistry() {
        CodedRegistry registry = new CodedRegistry();
        registry.register(ApprovalStatus.class);
        registry.register(DataSource.class);
        return registry;
    }
}

See source file for complete implementation.
```
This explains Art Universe's specific usage pattern.

---

## Cross-Reference Guidelines

### Cross-Reference Format

Use markdown links for all cross-references with descriptive text:

**Examples**:
```markdown
# Within same dimension
See the [Coded Enum Pattern](../entities/coded-enums.md) for details.

# Across dimensions
This implements the [Entity Binding Metafeature](../../metafeatures/entity-binding/README.md).

# To modules
Used in [Music Data Master Module](../../modules/mu-data-master/README.md).

# To devlogs
[Pipeline Metadata Task](../../devlogs/tasks/2025-12-04_pipeline-metadata/README.md)
```

**Link Text Guidelines**:
- Use descriptive text (not just filenames)
- Capitalize like titles
- Include document type when helpful (Pattern, Metafeature, Module)

### Bidirectional Links

**Rule**: If A references B, B must reference A

**Example**:

Pattern doc references devlog:
```markdown
## Origin

**Created in**: [Pipeline Metadata Persistence Task](../../devlogs/tasks/2025-12-04_pipeline-metadata/README.md)
```

Devlog references pattern:
```markdown
**Pattern created**: [Pipeline Versioning Pattern](../patterns/backend/pipeline-versioning.md)
```

---

## Quality Checklist

Before completing a devlog entry, verify:

### Content Completeness
- [ ] All three files created (conversation, requirements, implementation)
- [ ] Decision trail includes all major decisions
- [ ] All new patterns documented
- [ ] All modified patterns updated
- [ ] Cross-references are bidirectional

### Format Consistency
- [ ] Folder name follows `YYYY-MM-DD_{short-description}` format
- [ ] Filenames are exactly `conversation.md`, `requirements.md`, `implementation.md`
- [ ] Links are used for all cross-references
- [ ] Line counts within recommended ranges

### Decision Trail Quality
- [ ] Each decision has clear rationale
- [ ] Alternatives considered are documented
- [ ] Pattern/rule produced is linked
- [ ] Impact/constraints are noted

### Index Updated
- [ ] Entry added to devlogs/README.md
- [ ] Task list in reverse chronological order
- [ ] Statistics updated
- [ ] Patterns created are listed

### Bidirectional Links
- [ ] Devlog → Pattern references added
- [ ] Pattern → Devlog origin added
- [ ] Feature/Module → Devlog references added (if applicable)

## Remember

**Goal**: Future Claude (or human) should be able to:
1. Understand WHAT was done
2. Understand WHY decisions were made
3. Find WHERE patterns originated
4. Trace decision evolution over time

**Focus**: Decisions and their rationale, not exhaustive transcripts.

**Critical Rule**: When a devlog documents a decision that produces a pattern/rule/best practice, that pattern/rule/practice MUST reference the devlog entry. This bidirectional linking creates a decision trail showing WHY patterns exist.

---

## Scenarios

For example scenarios, see [Devlog Scenarios](devlog-scenarios.md)

## Troubleshooting

For troubleshooting scenarios, see [Devlog Troubleshooting](devlog-troubleshooting.md)