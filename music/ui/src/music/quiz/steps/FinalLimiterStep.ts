import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto, FinalLimiterStepConfig } from '../types/pipeline-steps';
import { parseStepConfig } from '../types/pipeline-steps';

export class FinalLimiterStep extends BaseStep {
  getType(): PipelineStepType {
    return 'FINAL_LIMITER';
  }

  getLabel(): string {
    return 'Final Limiter';
  }

  getDescription(): string {
    return 'Limits the final track list to a specific target number of tracks.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return true;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'FINAL_LIMITER', targetCount: 10 };
  }

  getConfigSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as FinalLimiterStepConfig;
    const count = config.targetCount || 10;
    return `Limit to ${count} tracks`;
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as FinalLimiterStepConfig;
    return `Limit ${config.targetCount || 10}`;
  }
}
