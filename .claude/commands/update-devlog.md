---
description: Create or update devlog documentation for current session
---

You are creating or updating devlog documentation for the current development session.

## Instructions

Follow these steps carefully:

### Step 1: Read the Guide

Read the devlog update guide, if not already:
- [`docs/kb/devlogs/DEVLOG_UPDATE_GUIDE.md`](../../docs/kb/devlogs/DEVLOG_UPDATE_GUIDE.md)

You goals are to:
- analyze this session
- plan changes including
  - create/update documentation for the features/fixes/tasks performed in current session.
  - create/update the non-devlog knowledge base
- get user's approval or correct the plan if requested
- apply the planned updates

### Step 2: Analyze the Session

Analyze what was accomplished in this session:

1. **Identify distinct features/fixes**:
   - Count how many separate features were implemented
   - Determine if they should be one task or multiple tasks

2. **Identify decisions made**:
   - What patterns were created or modified?
   - What architectural choices were made?
   - What metafeatures were created or modified?

3. **Identify documentation created/modified**:
   - pattern docs?
   - feature docs?
   - module docs?

4. **If the previous steps were not sufficient, review git status and today's commits**:
   ```bash
   git status
   git log --oneline --since="today"
   ```
   
### Step 3: Plan the knowledge base update

Answer the questions:
- should I document a new metafeature or update an existing one?
- should I document a new pattern or update an existing one?
- should I create a new guide or update an existing one?
- should I create new module doc or update an existing one?

For every 'Yes', include creating/updating the corresponding piece of documentation in the knowledge base into the plan.

### Step 4: Set the plan for approval

Present your plan to the user. If multiple options of plan are available, present them all with a couple of pros & cons.

Wait for user approval before continuing.
If corrections were suggested by user, apply them and set the update plan for approval again, until approved or canceled.

### Step 5: Create Devlog Files

For each approved task:

1. **Create task folder**:
   ```
   docs/kb/devlogs/tasks/YYYY-MM-DD_{short-description}/
   ```

2. **Create conversation.md**:
   - Follow template in DEVLOG_UPDATE_GUIDE.md section on conversation.md
   - Focus on decision points, not full transcript
   - Include phases: Context, Exploration, Design, Validation, Outcomes

3. **Create requirements.md**:
   - Follow template in DEVLOG_UPDATE_GUIDE.md section on requirements.md
   - Include original requirements (verbatim from user)
   - Include refined requirements with acceptance criteria
   - Mark completion status

4. **Create implementation.md**:
   - Follow template in DEVLOG_UPDATE_GUIDE.md section on implementation.md
   - **CRITICAL**: Include detailed Decision Trail section
   - List all patterns created/modified with @references
   - List all files changed
   - Cross-reference features/modules/related tasks

### Step 7: Update Cross-References

For each NEW pattern created:

1. **Add origin section to pattern doc**:
   ```markdown
   ## Origin

   **Created in**: @devlogs/tasks/YYYY-MM-DD_task-name/

   **Decision Context**: {Why this pattern was created}
   ```

2. **Update pattern from devlog**:
   - In implementation.md, reference: `@patterns/.../pattern-name.md`

For each EXISTING pattern modified:

1. **Add evolution section to pattern doc**:
   ```markdown
   ## Evolution

   ### Extension: {What was extended}

   **Added in**: @devlogs/tasks/YYYY-MM-DD_task-name/
   ```

2. **Update pattern from devlog**:
   - In implementation.md, reference: `@patterns/.../pattern-name.md`

### Step 6: Update Devlog Index

Update `docs/kb/devlogs/README.md`:

1. **Add task to task list** (in reverse chronological order):
   ```markdown
   ### YYYY-MM-DD

   - **[{short-description}](@devlogs/tasks/YYYY-MM-DD_{short-description}/)** - {One-sentence description}
     - Type: {type}
     - Modules: {modules}
     - (If applicable) Patterns Involved: {patterns}
     - Status: ✅ Completed | ⏳ In Progress
   ```

### Step 8: Confirm Completion

Present summary to user:

```
## Devlog Entry Created ✅

**Task**: {short-description}
**Location**: docs/kb/devlogs/tasks/YYYY-MM-DD_{short-description}/

**Files Created**:
- conversation.md ({N} lines)
- requirements.md ({N} lines)
- implementation.md ({N} lines)

**Cross-References Updated**:
- {N} pattern docs updated with origin/evolution
- Devlog index updated

**Patterns Documented**:
- {pattern1} - @patterns/.../
- {pattern2} - @patterns/.../

You can view the devlog at: @devlogs/tasks/YYYY-MM-DD_{short-description}/
```

## Common Scenarios

### Scenario: User says "update devlog for current session"

Follow steps 1-7 above.

### Scenario: User says "update devlog for task X"

1. Confirm: "Update existing devlog @devlogs/tasks/YYYY-MM-DD_X/?"
2. If yes, read existing files
3. Add "## Update: {new-date}" section to conversation.md
4. Update implementation.md with new changes
5. Update requirements.md if scope changed

### Scenario: User provides specific task name

Use the provided name, but verify it follows conventions:
- Lowercase
- Hyphens (not underscores)
- Under 50 characters
- Descriptive

If it doesn't, suggest correction.

## Quality Checks

Before confirming completion, verify:

- [ ] All three files created
- [ ] Decision trail complete in implementation.md
- [ ] All pattern cross-references bidirectional
- [ ] Devlog index updated
- [ ] Statistics updated
- [ ] Task naming follows conventions
- [ ] Line counts reasonable (conversation < 1000, requirements < 500, implementation < 800)

## Remember

**Goal**: Create decision-focused documentation that enables future understanding of WHY choices were made, not just WHAT was implemented.

**Focus**: Decisions, rationale, patterns created, not exhaustive transcripts.

**Critical Rule**: When documenting decisions that produce patterns/rules/practices, BOTH the devlog AND the pattern/rule/practice documentation MUST reference each other. This bidirectional linking is mandatory and creates the decision trail.
