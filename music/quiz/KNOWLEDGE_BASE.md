# Music Universe - Quiz Service

> **See also**: [Development Guide](../../DEVELOPMENT.md) | [Architecture Overview](../../ARCHITECTURE.md)

## Module Purpose

Quiz generation service that creates music quizzes from approved data. Focuses on "Guess the Track" game where participants listen to tracks and guess artist/title.

## Current Status

**🔄 In Development** - Basic Spring Boot application structure with health endpoint.

## Planned Features

### Quiz Generation
- Generate random track sets from approved data
- Configurable difficulty levels and categories
- Support for different quiz types

### Quiz Management
- Create and manage quiz templates
- Track quiz statistics and performance
- User session management

### API Endpoints (Planned)
- `GET /api/v1/quizzes` - List available quizzes
- `POST /api/v1/quizzes` - Create new quiz
- `GET /api/v1/quizzes/{id}/questions` - Get quiz questions
- `POST /api/v1/quizzes/{id}/answers` - Submit quiz answers

## Database Schema (Planned)

### Quiz Entities
- **Quiz**: Quiz templates and configurations
- **QuizQuestion**: Individual questions with tracks
- **QuizSession**: User quiz sessions
- **QuizAnswer**: User responses and scoring

## Development

**Local Development:**
```bash
# Run from project root directory
./scripts/run-module-dev.sh music-universe:music-quiz
# Runs on port 7083 with dev profile
```

**Docker Deployment:**
```bash
# Run from project root directory
./env/docker/deploy.sh local   # Port 9083
./env/docker/deploy.sh prod    # Port 8083
```

## Configuration

### Environment Variables
- `MU_QUIZ_DB_*` - Database connection parameters
- `MU_QUIZ_APP_*` - Application server configuration

### Database
- **Schema**: `mu_quiz` in PostgreSQL (shared with music-data)
- **Connection Pool**: HikariCP (max 10 connections)
- **Migrations**: Liquibase XML changelogs

## Integration Points

- **Music Data Service** - Consumes approved entities for quiz generation
- **Music Universe UI** - Future quiz management interface
- **PostgreSQL** - Shared database with `mu_quiz` schema
