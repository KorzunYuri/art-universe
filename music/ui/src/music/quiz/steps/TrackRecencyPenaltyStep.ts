import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto } from '../types/pipeline-steps';

export class TrackRecencyPenaltyStep extends BaseStep {
  getType(): PipelineStepType {
    return 'TRACK_RECENCY_PENALTY';
  }

  getLabel(): string {
    return 'Track Recency Penalty';
  }

  getDescription(): string {
    return 'Applies a penalty to tracks that were recently played, reducing their selection probability.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return false;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'TRACK_RECENCY_PENALTY' };
  }

  getConfigSummary(step: PipelineStepDto): string {
    return 'Track recency penalty applied';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    return 'Track recency';
  }
}
