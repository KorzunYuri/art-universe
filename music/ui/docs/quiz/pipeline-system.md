# Quiz Pipeline System

## Overview

Visual pipeline editor for creating multi-step quiz generation workflows with filters, transformations, and output formatting.

## Pipeline Concepts

**Pipeline:** Ordered sequence of processing steps

**Step:** Individual operation on track collection

**Execution:** Sequential processing through all steps

**Step Types & Backend Implementation:**

For complete documentation of available steps, their logic, and configuration, see:
- [Quiz Pack Generation Pipeline](../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Feature overview and step index
- [Pipeline Steps](../../../quiz/docs/pipeline/steps/README.md) - Detailed backend implementation for each step

## Visual Editor

**Technology:** @xyflow/react (React Flow)

**Features:**
- Visual step representation
- Drag-and-drop step reordering
- Step configuration panels
- Pipeline execution preview

**Interaction:**
- Add step: Click "Add Step" button
- Configure step: Click step to open config panel
- Reorder steps: Drag step to new position
- Delete step: Click delete icon on step

## Step Configuration

**Pattern:**
- Each step type has configuration form
- Form shows relevant options for step type
- Changes update pipeline metadata
- Validation prevents invalid configurations

**Example - Filter by Category:**
- Select category from dropdown
- Category populated via EntityPicker
- Min/max play count (optional)

**Example - Balance by Year:**
- Enter max tracks per year
- Year range selection (optional)

## Pipeline Metadata

**Stored Data:**
- Pipeline name
- Step sequence
- Step configurations
- Creation/modification dates

**Persistence:**
- Saved to backend via API
- Loaded when editing existing pipeline

## Pipeline Execution

**Process:**
1. User clicks "Execute" button
2. Pipeline sent to backend
3. Backend processes steps sequentially
4. Track list generated
5. Results returned to frontend
6. User previews generated quiz

**Preview Mode:**
- Shows track count after each step
- Helps tune step parameters
- No actual quiz created

## Components

**Main Components:**
- [PipelineEditor](../components/quiz/pipeline-editor.md) - Visual editor
- [StepBuilder](../components/quiz/step-builder.md) - Step configuration

**Supporting:**
- [EntityPicker](../components/shared/entity-picker.md) - Category selection in filters

## API Integration

**Mutations:**
- Save pipeline metadata
- Execute pipeline
- Delete pipeline

**Queries:**
- Load pipeline by ID
- List all pipelines
- Preview execution results

## Use Cases

**Example Pipeline Configuration:**

1. Start Datasource - Initialize with master tracks
2. Approved Filter - Only include bound tracks
3. Blacklist Filter - Exclude unwanted categories
4. Whitelist Filter - Focus on specific categories with weights
5. Artist Diversity - Prevent artist domination
6. Artist/Track Recency Penalty - Avoid recent repeats
7. Categories Balancer or Limiter - Final selection

See [Quiz Pack Generation Pipeline](../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) for detailed step documentation and configuration examples.

## Benefits

**Flexibility:**
- Customize quiz generation logic
- Reuse pipelines for similar quizzes
- Experiment with different configurations

**Visual:**
- See pipeline structure at glance
- Easy to understand and modify
- Non-technical users can create pipelines

**Extensibility:**
- Add new step types easily
- Compose complex logic from simple steps
- Maintain separation of concerns

## Related Documentation

- [Quiz Pack Generation Pipeline](../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Backend pipeline implementation and step documentation
- [Package Structure](../package-structure.md) - Quiz module organization
- [Entity Types](../patterns/data-types/entity-types.md) - Quiz entities
