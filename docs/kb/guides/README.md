# Guides - Entry Point

This is the starting point for understanding and working on the project.


## Quick Start (5-Minute Orientation)

### 1. Understand the System

**What is Art Universe?**
A three-stage data platform: **Raw Data Collection → Master Data Curation → Application**

**Read**: [Architecture Overview](architecture-overview.md) - LLM-optimized summary

**Deep dive**: [ARCHITECTURE.md](../../ARCHITECTURE.md) - Complete architectural details

### 2. Find What You Need

**By task type:**

| I want to... | Go to... |
|--------------|----------|
| **Understand architecture** | [ARCHITECTURE.md](../../ARCHITECTURE.md) |
| **Start development** | [DEVELOPMENT.md](../../DEVELOPMENT.md) |
| **Find a module** | [MODULES.md](gradle-commands) |
| **Look up service/port** | [SERVICES.md](../../SERVICES.md) |
| **Learn a pattern** | [Patterns](../patterns/README.md) |
| **Work on specific module** | [Modules](../../MODULES.md) |

**By knowledge dimension:**

| Dimension | Purpose | Start Here |
|-----------|---------|------------|
| **Guides** (you are here) | Project-wide orientation and workflows | This README |
| **Patterns** | How we build things (SOPs) | [patterns/README.md](../patterns/README.md) |
| **Modules** | Module-specific deep-dives | [modules/README.md](../../MODULES.md) |
| **Devlogs** | Decision history | [devlogs/README.md](../devlogs/README.md) |

### 3. Common First Tasks

**Understanding the codebase:**
1. Read [Architecture Overview](architecture-overview.md)
2. Check [MODULES.md](gradle-commands) for module list
3. Browse module READMEs in the codebase

**Starting development:**
1. Read [DEVELOPMENT.md](../../DEVELOPMENT.md) - environment setup
2. Check [SERVICES.md](../../SERVICES.md) - service ports
3. Review [Development Tasks](development-tasks.md) - workflows

**Working on a feature:**
1. Check [Patterns](../patterns/README.md) for implementation patterns

---

## Available Guides

### Architecture Overview
**File**: [architecture-overview.md](architecture-overview.md)

**What it covers:**
- High-level system architecture
- Three-stage pipeline (Collection → Curation → Application)
- Module structure and data flow
- Technology stack
- Service ports (quick reference)
- Design decisions and rationale

**Use when:** Understanding the big picture, architectural decisions

**For complete details:** See [ARCHITECTURE.md](../../ARCHITECTURE.md)

---

### Development Tasks
**File**: [development-tasks.md](development-tasks.md)

**What it covers:**
- Task-oriented development workflows
- File reading strategies
- Before/during/after development checklists
- Module navigation
- Git workflows
- Gradle best practices
- Common development workflows

**Use when:** Daily development, implementing features, fixing bugs

**For complete details:** See [DEVELOPMENT.md](../../DEVELOPMENT.md)

---

### Troubleshooting
**File**: [troubleshooting.md](troubleshooting.md)

**What it covers:**
- Common build issues
- Test failures
- Docker/deployment problems
- Database connection issues
- Port conflicts

**Use when:** Encountering errors or unexpected behavior

---

## The Two Documentation Tiers

Art Universe uses a two-tier documentation strategy:

### Tier 1: Root Docs (Comprehensive Reference)
**Location:** `docs/*.md`
**Purpose:** Single source of truth, complete details
**Audience:** Humans and LLMs needing comprehensive context

| Document | Content |
|----------|---------|
| [ARCHITECTURE.md](../../ARCHITECTURE.md) | Complete architecture, patterns, design decisions |
| [DEVELOPMENT.md](../../DEVELOPMENT.md) | Complete development guide, all procedures |
| [MODULES.md](gradle-commands) | Complete module catalog, dependencies |
| [SERVICES.md](../../SERVICES.md) | Complete service registry, all ports |

### Tier 2: KB Guides (LLM Entry Points)
**Location:** `docs/kb/guides/*.md` (you are here)
**Purpose:** Quick-start, task-oriented workflows with links to root docs
**Audience:** LLMs loading focused context

**Key principle:** KB guides link to root docs for complete information

---

## When to Update Documentation

After implementing changes, check if you need to update documentation:

### Check Root Docs (Source of Truth)

**Update [ARCHITECTURE.md](../../ARCHITECTURE.md) when:**
- Module structure changes
- Data flow changes (Collection → Curation → Application)
- New database schema added
- API patterns change
- Security/monitoring changes
- Technology stack updates

**Update [DEVELOPMENT.md](../../DEVELOPMENT.md) when:**
- Environment setup changes
- New scripts added
- Service ports change
- Build commands change
- IDE requirements change

**Update [MODULES.md](gradle-commands) when:**
- Module added/removed/renamed
- Dependencies change
- Technology versions updated

**Update [SERVICES.md](../../SERVICES.md) when:**
- Service added/removed
- Port changes in ANY environment
- API endpoints change
- Monitoring configuration changes

### Check KB Docs

**Update [Patterns](../patterns/README.md) when:**
- Creating reusable pattern used 3+ times
- Modifying existing pattern implementation

**Update [Modules](../../MODULES.md) when:**
- Module with KB docs is modified significantly

**Update Guides when:**
- Root docs updated (sync summaries)
- New common workflow identified
- Troubleshooting solution found

---

## Update Process

1. **Update root docs FIRST** (they are source of truth)
2. **Update KB guides** to reflect root doc changes
3. **Verify bidirectional links** are maintained
4. **Run `/update-devlog`** to document the work

---

## Navigation Tips

### Finding Information Quickly

**Use the right entry point:**
- Need ports? → [SERVICES.md](../../SERVICES.md)
- Need module list? → [MODULES.md](gradle-commands)
- Need architecture? → [ARCHITECTURE.md](../../ARCHITECTURE.md) or [architecture-overview.md](architecture-overview.md)
- Need how-to? → [development-tasks.md](development-tasks.md)

**Use knowledge dimensions:**
- How to implement? → [Patterns](../patterns/README.md)
- Module details? → [Modules](../../MODULES.md)
- Why this decision? → [Devlogs](../devlogs/README.md)

**Cross-references:**
All documentation uses markdown links with descriptive text. Follow links to navigate between related concepts.

---

## See Also

**Main Documentation:**
- [ARCHITECTURE.md](../../ARCHITECTURE.md) - Complete architectural details
- [DEVELOPMENT.md](../../DEVELOPMENT.md) - Complete development guide
- [MODULES.md](gradle-commands) - Complete module listing
- [SERVICES.md](../../SERVICES.md) - Service ports and endpoints

**Knowledge Base:**
- [KB Root](../README.md) - Documentation structure overview
- [Patterns](../patterns/README.md) - Implementation patterns
- [Modules](../../MODULES.md) - Module-specific docs
- [Devlogs](../devlogs/README.md) - Development history
