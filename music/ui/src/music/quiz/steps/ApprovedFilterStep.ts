import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto } from '../types/pipeline-steps';

export class ApprovedFilterStep extends BaseStep {
  getType(): PipelineStepType {
    return 'APPROVED_FILTER';
  }

  getLabel(): string {
    return 'Approved Filter';
  }

  getDescription(): string {
    return 'Filters the track list to include only tracks that have been manually approved.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return false;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'APPROVED_FILTER' };
  }

  getConfigSummary(step: PipelineStepDto): string {
    return 'Approved tracks only';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    return 'Approved only';
  }
}
