## Troubleshooting

This doc lists common scenarios for devlogs troubleshooting.

### "Session covered too much"

**Problem**: One session did 5+ distinct things

**Solution**:
1. Group related changes together
2. Propose 2-3 devlog entries
3. Get user approval for split

---

### "Unclear what pattern was created"

**Problem**: Decision made but pattern not documented

**Solution**:
1. Check if pattern already exists → Just reference it
2. If new pattern → Create pattern doc first, then link from devlog
3. If not pattern-worthy → Just document in decision trail without pattern reference

---

### "Conversation too long"

**Problem**: conversation.md approaching 1000 lines

**Solution**:
1. Compress exploration phase → List files, summarize findings
2. Compress routine implementation → Just list changes
3. Focus on decision points → Skip routine code reviews

---

### "Not sure if this needs a devlog"

**Decision Tree**:
1. Did it create new patterns/rules? → YES: Create devlog
2. Did it modify existing patterns? → YES: Create devlog
3. Is it a significant feature? → YES: Create devlog
4. Is it a bug fix that revealed insights? → YES: Create devlog
5. Is it purely applying existing patterns? → NO: Skip devlog
6. Is it trivial (typos, formatting)? → NO: Skip devlog

---