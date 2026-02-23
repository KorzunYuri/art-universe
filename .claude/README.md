# Claude Code Configuration for Art Universe

This directory contains Claude Code configuration for the Art Universe project.

## Structure

```
.claude/
├── README.md                         # This file
├── mcp.json                          # MCP server configuration
├── settings.local.json               # Local permissions and settings
└── commands/                         # Slash commands
```

## Elements

### 1. Knowledge Base

The `docs/kb/` directory in the project root contains LLM-optimized documentation organized into five dimensions:

- **features/** - Cross-cutting features with several services involved
- **patterns/** - Project-specific implementation patterns (HOW Art Universe uses tools/libs)
- **guides/** - Architecture overview, workflows, troubleshooting
- **devlogs/** - Development history and decision trails showing WHY patterns exist

**Navigation**: Documentation uses markdown links for all cross-references. 

**Getting Started**: Start with [kb/README.md](../docs/kb/README.md) for navigation guide and structure overview.

**Documenting Work**: Use `/update-devlog` after completing features/fixes to create decision trail documentation.

### 2. Available Commands

#### Documentation Commands
| Command               | Purpose                                                         |
|-----------------------|-----------------------------------------------------------------|
| `/update-devlog`      | Create/update development log for current session               |
| `/update-module-docs` | Create/update module docs that fit currently approved structure |
