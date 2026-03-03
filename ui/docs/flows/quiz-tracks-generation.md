# Quiz Tracks Pack Generation Workflow

## Overview

End-to-end workflow for generating curated track packs for quiz games using the visual pipeline editor and generation system.

## Workflow Steps

### 1. Create Game

- User navigates to Games page
- User clicks "Create Game" button
- System creates new game with empty pipeline
- User redirected to Game Details page

### 2. Configure Pipeline

**Pipeline Configuration:**
- Game has dedicated pipeline for track selection logic
- Pipeline consists of ordered sequence of steps
- Each step processes track collection and passes to next step

**Add Pipeline Steps:**
- User clicks "Add Step" button at desired position
- System shows step type selector
  - Position 0: Only INITIAL steps allowed (Start Datasource)
  - Position > 0: Only TRANSFORM steps allowed
- User selects step type from registry
- For config-free steps: Auto-saved immediately
- For configurable steps: Added to local state, requires configuration

**Configure Step:**
- User clicks step to view configuration panel
- Configure step-specific parameters (see [Pipeline System](../quiz/pipeline-system.md) for step types)
- Examples:
  - **Start Datasource**: Initialize with master tracks (no config)
  - **Approved Filter**: Include only bound/approved tracks
  - **Whitelist Filter**: Select categories with weights
  - **Artist Diversity**: Prevent artist domination
  - **Limiter/Balancer**: Final track selection (N tracks)
- User clicks "Save" to persist step configuration

**Reorder Steps:**
- User drags step to new position
- System updates pipeline order
- Step execution order determines final output

**Remove Steps:**
- User clicks delete icon on step
- System removes step from pipeline
- Remaining steps reordered automatically

**Validate Pipeline:**
- System validates pipeline structure
- Displays validation errors (blocking issues)
- Displays validation warnings (non-blocking suggestions)
- "Generate Tracks" button disabled if invalid

### 3. Execute Pipeline (Generate Tracks)

**Trigger Generation:**
- User clicks "Generate Tracks" button
- System validates pipeline (must be valid)
- Creates new Generation entity linked to game

**Backend Processing:**
- System creates immutable pipeline snapshot
- Executes steps sequentially from ord=0 to ord=N
- Each step:
  - Receives track dataset from previous step
  - Applies transformation/filtering logic
  - Outputs modified dataset
- Final step produces curated track list

**Generation Storage:**
- Generation includes:
  - Reference to game
  - Pipeline snapshot (immutable copy)
  - Generated track list
  - Status (PENDING, COMPLETED, FAILED)
  - Approval state (initially false)

**UI Update:**
- New generation appears in Generations List
- Sorted by creation date (newest first)
- Shows generation ID, track count, status

### 4. Review Generated Tracks

**View Generations:**
- User sees list of all generations for game
- Each generation shows:
  - Generation ID
  - Track count
  - Creation date
  - Approval status (✓ if approved)

**Select Generation:**
- User clicks generation in list
- System loads generation tracks
- Displays track details table with:
  - Track name
  - Artist name
  - Album name
  - Categories
  - Play count statistics

**View Pipeline Snapshot:**
- User clicks generation to open tab
- System loads generation's immutable pipeline
- Pipeline Editor shows read-only pipeline configuration
- User can review exact steps used for generation

**Individual Track Management:**
- User can delete unwanted tracks
- Click delete button on track row
- Track removed from generation
- Optimistic UI update with rollback on error

### 5. Approve Generation

**Approval Decision:**
- User reviews tracks in generation
- User clicks "Approve"

**Approve Generation:**
- Sets approved=true on generation
- Generation marked with a distinct style in list
- Approved generations are used for recency penalties
- Tracks from approved generations are excluded from future generations (e.g. via Track Recency Penalty step)

### 6. Iterate on Pipeline

**Refine Configuration:**
- User switches back to Game Pipeline tab
- Adjusts step configurations based on generation results
- Examples:
  - Adjust category weights in Whitelist Filter
  - Change target count in Limiter/Balancer
  - Add/remove penalty steps

**Generate Again:**
- User clicks "Generate Tracks" again
- Creates new generation with updated pipeline
- Previous generations preserved for comparison

**Compare Generations:**
- Multiple generation tabs can be open simultaneously
- User can compare different pipeline configurations
- Review which configuration produces best results

## Pipeline Tabs System

**Tab Types:**
- **Game Tab**: Active pipeline for game (editable)
- **Generation Tabs**: Pipeline snapshots (read-only)

**Tab Management:**
- Click generation to open new tab
- Switch between tabs to compare pipelines
- Close generation tabs when done reviewing
- Game tab always present and cannot be closed

## State Management

**Pipeline States:**
- Game pipeline: Mutable, can be edited
- Generation pipeline: Immutable snapshot

**Cache Management:**
- Pipelines cached separately from games
- Pipeline updates invalidate pipeline cache only
- Generation list updated optimistically on new generation

**Optimistic Updates:**
- Step configuration changes update UI immediately
- Server request sent in background
- Rollback on error with notification

## Components Involved

**Main Components:**
- [Games](../../src/music/quiz/components/Games.tsx) - Game list and creation
- [GameDetails](../../src/music/quiz/components/GameDetails/GameDetails.tsx) - Game management and orchestration
- [PipelineEditor](../../src/music/quiz/components/PipelineEditor/PipelineEditor.tsx) - Visual pipeline configuration
- [PipelineStepper](../../src/music/quiz/components/PipelineStepper/PipelineStepper.tsx) - Step visualization and reordering
- [PipelineStepDetail](../../src/music/quiz/components/PipelineStepDetail/PipelineStepDetail.tsx) - Step configuration panel
- [GenerationsList](../../src/music/quiz/components/GenerationsList/GenerationsList.tsx) - Generation list with approve/disapprove
- [GenerationTracks](../../src/music/quiz/components/GenerationTracks.tsx) - Track list viewer
- [PipelineTabs](../../src/music/quiz/components/PipelineTabs/PipelineTabs.tsx) - Tab management

## API Integration

**Game Endpoints:**
- `POST /api/v1/games` - Create game
- `GET /api/v1/games/{gameId}` - Get game with pipeline

**Generation Endpoints:**
- `POST /api/v1/games/{gameId}/generations` - Generate tracks
- `GET /api/v1/games/{gameId}/generations` - List generations
- `GET /api/v1/generations/{generationId}/tracks` - Get tracks
- `PATCH /api/v1/generations/{generationId}/approve` - Approve generation
- `PATCH /api/v1/generations/{generationId}/disapprove` - Disapprove generation
- `DELETE /api/v1/generations/{generationId}/tracks/{trackId}` - Delete track

**Pipeline Endpoints:**
- `GET /api/v1/pipeline/{pipelineId}` - Get pipeline
- `POST /api/v1/pipeline/{pipelineId}/steps` - Add step
- `PUT /api/v1/pipeline/{pipelineId}/steps/{stepId}/move` - Reorder step
- `DELETE /api/v1/pipeline/{pipelineId}/steps/{stepId}` - Remove step
- `PUT /api/v1/pipeline/{pipelineId}/steps/{stepId}` - Update step config
- `POST /api/v1/pipeline/{pipelineId}/steps/{stepId}/execute` - Execute single step

**Error Handling:**
- Network errors show notification
- Optimistic updates rollback on failure
- Validation errors prevent generation


## Related Documentation

- [Quiz Pipeline System](../quiz/pipeline-system.md) - Visual pipeline editor details
- [Quiz Pack Generation Pipeline](../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Backend pipeline implementation
- [Pipeline Steps](../../../quiz/docs/pipeline/steps/README.md) - Detailed step documentation
- [API Reference](../../../quiz/docs/api.md) - Complete API documentation
- [Entity Types](../patterns/data-types/entity-types.md) - Quiz entities (Game, Generation)
