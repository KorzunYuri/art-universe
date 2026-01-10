# Development Workflow

**Purpose**: Guide for daily development practices and workflows when working on the Art Universe project.

---

## Before Making Changes

### 1. Read Context Files

Before modifying any module, always read these files first:

**For all modules:**
- [Project Modules Index](../../MODULES.md) (lists all modules with their READMEs)
- Module README: `<module-path>/README.md` (each module has README in its root)
- Build configuration: `<module-path>/build.gradle` or `package.json`

**For Java/Spring Boot modules:**
- Docker configuration: `<module-path>/Dockerfile*`
- Common patterns: `common/src/main/java/**/*`
- Build sources: `buildSrc/src/main/groovy/*`

**For React/TypeScript modules:**
- Vite configuration: `<module-path>/vite.config.ts`
- TypeScript configuration: `<module-path>/tsconfig*.json`
- Environment variables: `<module-path>/.env`

### 2. Understand Module Structure

Use tools to explore:

```bash
# List directory structure (Windows)
ls -la <module-path>/

# Find key file types
# Use Glob tool with patterns like:
<module-path>/**/*.java
<module-path>/**/*.ts
<module-path>/**/*.tsx
```

### 3. Read Related Code

Before changing a file, understand its context:

- Services the file depends on
- Controllers that use the service
- Tests for the functionality
- Related entities/DTOs

### 4. Check Dependencies

```bash
# Read build configuration
Read <module-path>/build.gradle

# Look for dependencies block
dependencies {
    implementation project(':common:commons-web')
    ...
}
```

---

## During Development

### 1. Follow Module Patterns

Look at existing code for conventions:

- How are similar features implemented?
- What naming conventions are used?
- How is error handling done?
- What testing approach is used?

### 2. Run Tests Frequently

After making changes, run tests to verify behavior:

```bash
# Run module tests
./gradlew :<module>:test

# Run specific test class
./gradlew :<module>:test --tests "ArtistServiceTest"
```

**See**: [Gradle Commands](../guides/gradle-commands.md)

### 3. Check for Compilation

Ensure code compiles before committing:

```bash
# Build the module
./gradlew :<module>:build
```

### 4. Read Test Files

Understand expected behavior by reading tests:

```bash
# Find test files
Glob pattern "<module-path>/**/test/**/*Test.java"

# Read test class
Read <module-path>/src/test/java/.../ArtistServiceTest.java
```

### 5. Update Tests

Keep tests in sync with changes:

- Add tests for new functionality
- Update tests for modified behavior
- Ensure test names reflect actual behavior

**See**: [Testing Patterns](../patterns/backend/testing/overview.md)

---

## After Changes

### 1. Build the Module

```bash
./gradlew :<module>:build
```

### 2. Run All Tests

```bash
./gradlew :<module>:test
```

### 3. Check Git Diff

Review what changed:

```bash
# See changes in module
git diff <module-path>/

# See staged changes
git status
```

### 4. Verify Related Modules

If you changed shared code (common modules), build dependent modules:

```bash
# Example: After changing commons-jpa
./gradlew :music:data:master:build
./gradlew :music:quiz:build
```

### 5. Update Documentation

**IMPORTANT**: Before finishing, consider updating relevant documentation using /update-devlog slash command.

---

## File Reading Strategies

### Strategy 1: Top-Down

Start with high-level overview, drill down:

1. **README** - Understand module purpose
2. **Main application class** - Entry point
3. **Controllers/Services** - Main functionality
4. **Entities/Repositories** - Data layer
5**Configuration classes** - How it's configured

**When to use**: New to the module or understanding architecture

### Strategy 2: Feature-Focused

Focus on specific feature:

1. **Search**: "Where is feature X implemented?"
2. **Read controller/service** for the feature
3. **Read related entities**
4. **Read tests** for the feature

**When to use**: Working on specific feature or bug fix

### Strategy 3: Test-Driven

Understand through tests:

1. **Find test files**: `Glob pattern "**/*Test.java"`
2. **Read test class** for the feature
3. **Read implementation** files referenced in tests
4. **Understand through test assertions**

**When to use**: Understanding expected behavior or fixing failing tests

---

## Navigation Between Modules

### Finding Related Modules

**By dependency (build.gradle)**:
```bash
Read <module-path>/build.gradle
# Look at dependencies { } block
```

**By package structure**:
```bash
Glob pattern "music/data/raw/lastfm/**/*"
# Shows all LastFM-related modules
```

**By semantic search**:
```
"Which modules handle LastFM data?"
"Find all modules that depend on commons-jpa"
```

### Reading Common Code

When a module uses common utilities:

**Find common patterns**:
```bash
Glob pattern "common/**/src/main/java/**/*.java"
```

**Search for specific classes**:
```bash
Grep pattern "class.*BaseController" path "common/"
```

**Read common utilities**:
```bash
Read common/commons-web/src/main/java/yurykorzun/art/universe/common/web/BaseController.java
```

---

## Working with Git

### General Rule

**The developer handles reviews and commits.** AI is prohibited from doing anything except `git add` on changed files.

### Checking Status

```bash
# See current status
git status

# See recent commits
git log --oneline -10

# See changes in specific directory
git diff music/quiz/
```

### Common Workflows

**See what changed**:
```bash
git diff
git diff --staged
```

**View commit history**:
```bash
git log --oneline
git log --oneline --graph
```

---

## Gradle Best Practices

### Key Principle

**IMPORTANT**: Gradle Wrapper (`gradlew`/`gradlew.bat`) is located **only at project root**. All Gradle commands must be executed from project root, not from module roots.

### Common Commands

```bash
# Build module
./gradlew :<module-path>:build

# Run tests
./gradlew :<module-path>:test

# Run application (for runnable modules)
./gradlew :<module-path>:bootRun

# Clean build
./gradlew :<module-path>:clean build
```

**See**: [Gradle Commands](../guides/gradle-commands.md)

---

## Module Categories

Understanding module types helps determine workflow:

| Category | Characteristics | Examples |
|----------|----------------|----------|
| **Application Modules** | Runnable, have Dockerfile, have application.yml | music:data:master, music:quiz |
| **Library Modules** | Shared code, no Dockerfile | commons-jpa, lastfm-models |
| **ETL Modules** | Process data, scheduled jobs | lastfm-calls-generator |
| **Test Support** | Testing utilities | commons-test-db |

**See**: [Project Modules Index](../../MODULES.md)

---

## Development Environment

### Running Modules Locally

**Individual module (dev mode)**: watch [Development Guide](../../DEVELOPMENT.md) for reference

**Full environment (Docker)**:
```bash
./env/docker/deploy.sh local
# Or on Windows:
env\docker\deploy.bat local
```

### Stopping Services

```bash
./env/docker/stop.sh all
# Or on Windows:
env\docker\stop.bat all
```

---

## Documentation Updates

### When to Update Docs

- Added new feature or module
- Changed API endpoints
- Modified architecture or design
- Changed deployment process
- Updated dependencies

### What to Update

**In `docs/`** (main documentation):
- `ARCHITECTURE.md` - Architecture changes
- `MODULES.md` - New modules or module changes
- `DEVELOPMENT.md` - Development process changes

**In `docs/kb/`** (LLM documentation):
- Patterns - New or modified patterns
- Features - New features or feature changes
- Guides - Workflow or architecture changes

**In module roots** (Module-specific):
- `<module-path>/README.md` - Module documentation and usage

### Update Process

1. **Describe changes** - Concise, human- and LLM-readable
2. **Get user approval** - Don't update without approval
3. **Update docs** - Make the changes

---

## Common Workflows

### Adding a New Feature

1. Read architecture overview: [Architecture Overview](./architecture-overview.md)
2. Identify affected modules: [Project Modules Index](../../MODULES.md)
3. Review similar features: [Features](../features/README.md)
4. Check patterns to use: [Patterns](../patterns/README.md)
5. Implement feature
6. Write tests
7. Update documentation
8. Get user approval
9. Commit (user performs commit)

### Fixing a Bug

1. Reproduce the bug
2. Find relevant test
3. Read implementation
4. Identify root cause
5. Fix the issue
6. Update/add tests
7. Verify fix with tests
8. Check git diff
9. Get user approval
10. Commit (user performs commit)

### Refactoring Code

1. Read existing implementation
2. Understand current behavior
3. Ensure tests exist and pass
4. Make incremental changes
5. Run tests after each change
6. Verify no behavior change
7. Update documentation if needed
8. Get user approval
9. Commit (user performs commit)

---

## Quick Reference

| Task | Command/Action |
|------|----------------|
| **Read module** | Read `<module-path>/README.md` |
| **Build module** | `./gradlew :<module>:build` |
| **Run tests** | `./gradlew :<module>:test` |
| **Check status** | `git status` |
| **See changes** | `git diff` |
| **Find files** | Use Glob tool |
| **Search code** | Use Grep tool |
| **Run module locally** | `./scripts/run-module-dev.sh <module>` |

---

## See Also

- [Gradle Commands](../guides/gradle-commands.md) - Gradle commands
- [Testing Patterns](../patterns/backend/testing/overview.md) - Testing patterns
- [Architecture Overview](./architecture-overview.md) - System architecture
- [Project Modules Index](../../MODULES.md) - Module reference
- [Features Index](../features/README.md) - Feature documentation
- [Development Reference](../../DEVELOPMENT.md) - Complete development guide
