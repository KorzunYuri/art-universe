---
description: Analyze test coverage for a module and improve it
argument-hint: "<:module:path>"
---

You are analyzing and improving test coverage for a Gradle module in the Art Universe project.

## Input

The user provides the Gradle module path as the command argument, e.g. `:music:data:master`.
If no argument was provided, ask the user which module to analyze before continuing.

## Step 1: Run coverage task

Use the **gradle-task-executor** agent to run:
```
./gradlew <module-path>:jacocoSummary
```

This task runs tests, generates the JaCoCo XML report, then prints only classes below 80% line coverage in the format:
```
com/example/SomeClass -> 62% (missed: 15)
```

## Step 2: Parse the output

Read the log file returned by the executor. Extract:
- Each `CLASS_PATH -> COVERAGE% (missed: N)` line
- Sort by coverage ascending (worst first)

If the output is empty (no lines printed), coverage is already ≥ 80% across all classes — congratulate the user and stop.

## Step 3: Present the coverage report

Show a concise table:

```
Coverage Report — <module-path>
─────────────────────────────────────────────────────
Class                                   Coverage  Missed
com/example/foo/LowCoveredService         38%       24
com/example/bar/AnotherClass              55%       11
...
─────────────────────────────────────────────────────
X classes below 80% threshold
```

## Step 4: Ask for direction

Ask the user how to proceed:
1. **Analyze only** — explain what each low-coverage class does and what cases are likely missing
2. **Implement missing tests** — write the missing tests for selected (or all) classes
3. **Focus on specific class** — user picks a class to drill into

Wait for the user's choice before continuing.

## Step 5: Analyze coverage gaps (for options 1 or 2)

For each class to analyze:
1. Read the source file
2. Read existing test file(s) for this class (if any)
3. Read the JaCoCo XML report at `<module>/build/reports/jacoco/test/jacocoTestReport.xml` to find which specific methods/lines are uncovered for this class (look for `<method>` and `<line>` elements with `mi > 0`)
4. Identify the specific uncovered branches or methods

## Step 6: Implement tests (option 2)

For each class:
- Follow existing test conventions in the module (check `src/test/java/`)
- Check `docs/kb/patterns/README.md` for relevant testing patterns
- Write focused unit tests targeting the uncovered code paths
- Place tests in the correct package mirroring the source structure

After writing tests, run `jacocoSummary` again to confirm coverage improved.

## Constraints

- Do not modify production code to make it easier to test
- Follow the project's existing test patterns (MockMvc, Mockito, TestContainers as appropriate)
- Only add tests that are meaningful — avoid coverage padding with trivial assertions
