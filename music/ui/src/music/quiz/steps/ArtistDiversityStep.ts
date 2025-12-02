import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto } from '../types/pipeline-steps';

export class ArtistDiversityStep extends BaseStep {
  getType(): PipelineStepType {
    return 'ARTIST_DIVERSITY';
  }

  getLabel(): string {
    return 'Artist Diversity';
  }

  getDescription(): string {
    return 'Ensures variety by limiting the number of tracks from the same artist in the final selection.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return false;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'ARTIST_DIVERSITY' };
  }

  getConfigSummary(step: PipelineStepDto): string {
    return 'Artist diversity applied';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    return 'Artist diversity';
  }
}
