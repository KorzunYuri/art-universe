# Pattern Documentation Guide

This guide defines standards for documenting patterns in the Art Universe codebase.

## What Makes a Pattern "Project-Specific"?

Patterns document Art Universe-specific practices, NOT generic tool usage.

### The 4-Question Litmus Test

1. ✅ **Does it contain Art Universe-specific decisions?**
   - Generic tool knowledge → NOT a pattern
   - How WE use the tool → Pattern candidate

2. ✅ **Does it reference our actual codebase?**
   - Examples from our files → Pattern
   - Generic/hypothetical examples → NOT a pattern

3. ✅ **Would this be useful after the tool is learned?**
   - Tutorial content → NOT a pattern
   - Reusable SOP → Pattern

4. ✅ **Does it describe consistent practice across modules?**
   - Used in 3+ places consistently → Pattern
   - One-off approach → Feature documentation

### Examples

- ❌ "How to use @SpringBootApplication" - Generic Spring Boot, DELETE
- ✅ "How we configure CodedRegistry in Spring Boot" - Project-specific, KEEP
- ❌ "Introduction to SLF4J logging" - Generic tutorial, DELETE
- ✅ "Our profile-specific logging configuration" - Project decision, KEEP

## Documentation policies

### Template

Always use [Pattern Template](doc-templates/PATTERN_TEMPLATE.md) as target doc structure. If the template complies to a specific case poorly - consult with the user. 

### Code Example Policy

**Show JUST ENOUGH code to explain the pattern. Point to source files for complete examples.**

#### ✅ EMBED code when (5-15 lines max):
- Showing the key pattern
- Comparing before/after
- Template/skeleton for copy-paste
- Critical snippet that explains the concept

#### ❌ DON'T EMBED code when:
- Full class implementations (link to source instead)
- Boilerplate code (imports, getters, setters)
- Examples already in source
- Code that might change

#### Reference source code using lists

Example format:

- [ApprovalStatus](music/data/master/.../entity/coded/ApprovalStatus.java) - Status enum for master entities
- [ApiCallStatus](music/data/raw/lastfm/.../entity/coded/ApiCallStatus.java) - Status for ETL calls

See source files for complete implementations.
