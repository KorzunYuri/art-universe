## Common Scenarios

This doc lists common scenarios for devlogs updating.

### Scenario 1: Single Feature, Single Session

**Input**: One feature, 3 commits, completed in one session

**Output**:
- One devlog entry: `2025-12-04_feature-name/`
- Three files: conversation.md, requirements.md, implementation.md
- Git commits listed in conversation.md (optional)

---

### Scenario 2: Multi-Day Feature

**Input**: Same feature worked on 2025-12-03 and 2025-12-04

**Output**:
- One devlog entry: `2025-12-04_feature-name/` (use completion date)
- In conversation.md, note: "Started: 2025-12-03, Completed: 2025-12-04"

---

### Scenario 3: Session with Multiple Features

**Input**: Session adds Feature A and Feature B (unrelated)

**Output**:
- Two devlog entries:
    - `2025-12-04_feature-a/`
    - `2025-12-04_feature-b/`
- Each gets own conversation/requirements/implementation files
- Original session conversation gets split by feature

---

### Scenario 4: Updating Existing Entry

**Input**: Bug discovered in previous feature, fixed in new session

**Options**:

**Option A** (Small fix, same feature):
- Update existing devlog entry
- Add "## Update: 2025-12-05" section in conversation.md
- Update implementation.md with fix details

**Option B** (Significant fix, new pattern):
- Create new devlog entry: `2025-12-05_feature-name-bugfix/`
- Reference original: "Related to [Original Feature Task](../../devlogs/tasks/2025-12-04_feature-name/README.md)"

**Decision Guide**: If the fix creates new patterns/decisions, use Option B.

---

### Scenario 5: Documentation-Only Session

**Input**: Session only creates/updates documentation, no code changes

**Output**:
- One devlog entry: `2025-12-04_docs-migration/`
- Type: Documentation
- implementation.md focuses on documentation structure decisions
- Still include decision trail if doc structure creates patterns

---

### Scenario 6: Referencing MIGRATION_STATUS.md

**Input**: Documenting historical work from MIGRATION_STATUS.md

**Output**:
- Create retroactive devlog entries with "**Note**: Retroactive documentation"
- Use commit date for folder name if known
- Reference MIGRATION_STATUS: "Documented as part of docs migration effort"

---