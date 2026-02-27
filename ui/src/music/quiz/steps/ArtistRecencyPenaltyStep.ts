import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto } from '../types/pipeline-steps';

export class ArtistRecencyPenaltyStep extends BaseStep {
  getType(): PipelineStepType {
    return 'ARTIST_RECENCY_PENALTY';
  }

  getLabel(): string {
    return 'Artist Recency Penalty';
  }

  getDescription(): string {
    return 'Applies a penalty to tracks from artists that were recently played, reducing their selection probability.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return false;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'ARTIST_RECENCY_PENALTY' };
  }

  getConfigSummary(step: PipelineStepDto): string {
    return 'Artist recency penalty applied';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    return 'Artist recency';
  }
}
