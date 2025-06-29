# Scripts Directory

This directory contains utility scripts for the Art Universe project.

## run-module-dev.sh / run-module-dev.bat

Cross-platform scripts for running individual modules locally with proper environment variable loading.

### Usage

```bash
# Unix/Linux/macOS/WSL/Git Bash
./scripts/run-module-dev.sh <module-path> [additional-gradle-args]

# Windows Command Prompt
scripts\run-module-dev.bat <module-path> [additional-gradle-args]
```

### Examples

```bash
# Run music-data-raw-lastfm module
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm

# Run music-data module
./scripts/run-module-dev.sh music-universe:music-data

# Run music-quiz module
./scripts/run-module-dev.sh music-universe:music-quiz

# Run with additional Gradle arguments
./scripts/run-module-dev.sh music-universe:music-data --debug
```

### Available Modules

| Module Path | Description | Default Port |
|-------------|-------------|--------------|
| `music-universe:music-data-raw-lastfm` | LastFM raw data collection service | 7081 |
| `music-universe:music-data` | Curated music data management service | 7082 |
| `music-universe:music-quiz` | Music quiz generation service | 7083 |

### Environment Variable Loading Order

The script loads environment variables in the following order (later values override earlier ones):

1. **Docker local configuration**: `env/docker/local/[module-name].env`
2. **Docker local secrets**: `env/docker/local/[module-name].secrets.env`
3. **Development overrides**: `[module-path]/dev.override.env`

### Configuration Files Mapping

### Configuration Files Mapping

| Module Path | Module Name | Config Files |
|-------------|-------------|--------------|
| `music-universe:music-data-raw-lastfm` | `music-data-raw-lastfm` | `music-data-raw-lastfm.env`, `music-data-raw-lastfm.secrets.env` |
| `music-universe:music-data` | `music-data` | `music-data.env`, `music-data.secrets.env` |
| `music-universe:music-quiz` | `music-quiz` | `music-quiz.env`, `music-quiz.secrets.env` |
| `music-universe:music-universe-ui` | `music-universe-ui` | `music-universe-ui.env`, `music-universe-ui.secrets.env` |

### Development Override Files

Each module can have a `dev.override.env` file in its directory that overrides Docker configuration for local development:

- `music-universe/music-data-raw-lastfm/dev.override.env`
- `music-universe/music-data/dev.override.env`
- `music-universe/music-quiz/dev.override.env`

These files typically configure:
- Database connections to localhost instead of Docker containers
- Different port numbers to avoid conflicts
- Development-specific settings

### Features

- **Cross-platform compatibility**: Works on Windows, macOS, Linux, WSL, and Git Bash
- **Automatic environment loading**: Loads configuration files in the correct order
- **Error handling**: Provides clear feedback about missing configuration files
- **Spring profile activation**: Automatically sets `spring.profiles.active=dev`
- **PROJECT_ROOT support**: Sets the PROJECT_ROOT environment variable for modules that need it
- **Fixed Windows compatibility**: Proper argument handling and Gradle command formation

### Requirements

- Must be run from the project root directory (where `gradlew` is located)
- Module must have a valid Gradle configuration with `bootRun` task
- Configuration files should exist in `env/docker/local/` directory

### Troubleshooting

#### "Script must be run from the project root!"
Make sure you're running the script from the directory containing `gradlew`.

#### "No main .env file found"
This is normal if the module doesn't have a main configuration file. The script will continue with available files.

#### Module not found
Check that the module path is correct using:
```bash
./gradlew projects
```

#### Port conflicts
If you get port binding errors, make sure no other services are running on the same ports, or modify the port configuration in the `.env` files.

#### Windows-specific issues
- Ensure you're using `scripts\run-module-dev.bat` (not `.sh`) in Command Prompt
- The script automatically handles argument parsing and Gradle command formation
- If you see Gradle task errors, verify the module path matches exactly: `music-universe:music-data-raw-lastfm`
