# Music Quiz API Reference

This document lists endpoints available in Music Quiz web app.

## Base URL

All endpoints are prefixed with `/api/v1` unless otherwise noted.

## Endpoint Groups

- [Game Management](#game-management)
- [Generation Operations](#generation-operations)
- [Pipeline Manipulation](#pipeline-manipulation)
- [Entity Binding](#entity-binding)
- [Health Check](#health-check)


## Game Management

**Base Path:** `/api/v1/games`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| POST | `/` | Create new game with pipeline | - | [GameWithPipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GameWithPipelineDto.java) |
| GET | `/` | List all games (pageable) | Query: `pageable` | Page<[GameDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GameDto.java)> |
| GET | `/{gameId}` | Get game with pipeline | Path: `gameId` | [GameWithPipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GameWithPipelineDto.java) |


## Generation Operations

Endpoints for generating and managing quiz track generations.

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| POST | `/api/v1/games/{gameId}/generations` | Generate tracks for game | Path: `gameId` | [GenerationDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GenerationDto.java) |
| GET | `/api/v1/games/{gameId}/generations` | List game generations | Path: `gameId` | List<[GenerationDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GenerationDto.java)> |
| GET | `/api/v1/generations/{generationId}/tracks` | Get generation tracks | Path: `generationId` | List<[GenerationTrackDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GenerationTrackDto.java)> |
| PATCH | `/api/v1/generations/{generationId}/approve` | Approve generation | Path: `generationId` | [GenerationDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GenerationDto.java) |
| PATCH | `/api/v1/generations/{generationId}/disapprove` | Disapprove generation | Path: `generationId` | [GenerationDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/GenerationDto.java) |
| DELETE | `/api/v1/generations/{generationId}/tracks/{trackId}` | Remove track from generation | Path: `generationId`, `trackId` | `void` |
| GET | `/api/v1/generations/{generationId}/pipeline` | Get generation's pipeline snapshot | Path: `generationId` | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |


## Pipeline Manipulation

Endpoints for managing game pipeline configuration and steps.

**Base Path:** `/api/v1/pipeline`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| GET | `/{pipelineId}` | Get pipeline with steps | Path: `pipelineId` | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |
| POST | `/{pipelineId}/steps` | Add step at position | Path: `pipelineId`<br>Query: `position`<br>Body: [PipelineStepDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineStepDto.java) | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |
| PUT | `/{pipelineId}/steps/{stepId}/move` | Move step to new position | Path: `pipelineId`, `stepId`<br>Query: `newPosition` | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |
| DELETE | `/{pipelineId}/steps/{stepId}` | Remove step (soft delete) | Path: `pipelineId`, `stepId` | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |
| PUT | `/{pipelineId}/steps/{stepId}` | Update step configuration | Path: `pipelineId`, `stepId`<br>Body: [PipelineStepDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineStepDto.java) | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |

**Base Path:** `/api/v1/steps`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| GET | `/{stepId}/preview` | Get step preview | Path: `stepId` | `String` |
| POST | `/api/v1/pipeline/{pipelineId}/steps/{stepId}/execute` | Execute single step | Path: `pipelineId`, `stepId` | [PipelineDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/PipelineDto.java) |


## Entity Binding

Endpoints for binding/unbinding master entities (artists, tracks) to the quiz database.

### Artist Bindings

**Base Path:** `/api/v1/artists`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| POST | `/{masterId}/bind` | Bind artist to quiz | Path: `masterId` | [BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java) |
| DELETE | `/{masterId}/bind` | Unbind artist from quiz | Path: `masterId` | [BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java) |
| GET | `/{masterId}/binding` | Get binding status | Path: `masterId` | [BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java) |
| POST | `/bindings` | Get multiple artist bindings | Body: `List<Long>` (master IDs) | List<[BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java)> |

### Track Bindings

**Base Path:** `/api/v1/tracks`

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| POST | `/{masterId}/bind` | Bind track to quiz | Path: `masterId` | [BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java) |
| DELETE | `/{masterId}/bind` | Unbind track from quiz | Path: `masterId` | [BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java) |
| GET | `/{masterId}/binding` | Get binding status | Path: `masterId` | [BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java) |
| POST | `/bindings` | Get multiple track bindings | Body: `List<Long>` (master IDs) | List<[BindingDto](../src/main/java/yurykorzun/art/universe/music/quiz/dto/BindingDto.java)> |


## Health Check

| Method | Path | Purpose | Input | Response DTO |
|--------|------|---------|-------|--------------|
| GET | `/health` | Service health check | - | `Map<String, String>` {"status": "UP"} |


## Common Patterns

### Pagination

Search endpoints support Spring Data pagination parameters:
- `page`: Page number (0-based)
- `size`: Page size
- `sort`: Sort criteria (e.g., `name,asc`)

For more details, see the [REST API Conventions](../../../docs/kb/patterns/backend/api/conventions.md).