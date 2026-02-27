# Gradle Integration for art-universe-ui

## Overview
The `art-universe-ui` module is integrated with Gradle build system to provide seamless build and cleanup operations.

## Available Tasks

### Build Tasks
- `npmInstall` - Install npm dependencies
- `npmBuild` - Build React application (depends on npmInstall)
- `build` - Standard Gradle build (executes npmBuild)
- `assemble` - Standard Gradle assemble (executes npmBuild)

### Clean Tasks
- `npmClean` - Clean npm build artifacts (removes dist/)
- `clean` - Standard Gradle clean (executes npmClean)

## Usage

### Build the UI module
```bash
./gradlew :ui:build
```

### Clean the UI module
```bash
./gradlew :ui:clean
```

### Build entire project (includes UI)
```bash
./gradlew build -x test
```

### Clean entire project (includes UI)
```bash
./gradlew clean
```

## Integration Details

- **Inputs**: `src/`, `package.json`, `vite.config.ts`, `tsconfig.json`, etc.
- **Outputs**: `dist/` directory with built React application
- **Dependencies**: `npmBuild` depends on `npmInstall`
- **Lifecycle**: Integrated with standard Gradle `build` and `clean` tasks
- **JAR disabled**: Since this is a frontend module, JAR creation is disabled

## Windows Compatibility & Troubleshooting

### Symlink Issues
The build configuration handles Windows symlink issues in `node_modules/.bin/`:
- `node_modules` is excluded from Gradle's file tracking to avoid symlink errors
- Uses custom `upToDateWhen` logic instead of tracking `node_modules` as output

### Common Issues

#### "Cannot snapshot ... not a regular file" Error
This error occurs when Gradle tries to track symbolic links in `node_modules/.bin/`. The current configuration avoids this by:
- Not declaring `node_modules` as task output
- Using custom up-to-date checking based on file timestamps

#### Build Cache Issues
If you encounter I/O errors or cache corruption:
```bash
# Stop Gradle daemon
./gradlew --stop

# Clean Gradle cache (if needed)
rm -rf ~/.gradle/caches/

# Clean and rebuild
./gradlew clean build -x test
```

## Technical Implementation

### OS Detection
- Automatically uses `npm.cmd` on Windows, `npm` on Unix systems
- Configures copy tasks to handle Windows-specific file system issues

### Caching Strategy
- Minimal file tracking to avoid symlink issues
- Custom up-to-date logic based on `package-lock.json` timestamps
- Input files tracked: source code, config files, package definitions

## Notes

- Uses OS-appropriate npm command (npm.cmd on Windows, npm on Unix)
- Includes proper input/output declarations for Gradle caching
- No test integration yet - tests will be added later
- Build artifacts are cleaned properly with `npmClean` task
- Symlink issues resolved for Windows WSL environments
