import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto, BlacklistFilterStepConfig } from '../types/pipeline-steps';
import { parseStepConfig } from '../types/pipeline-steps';

export class BlacklistFilterStep extends BaseStep {
  getType(): PipelineStepType {
    return 'BLACKLIST_FILTER';
  }

  getLabel(): string {
    return 'Blacklist Filter';
  }

  getDescription(): string {
    return 'Removes tracks belonging to specific categories from the track list.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return true;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'BLACKLIST_FILTER', categoryIds: [] };
  }

  getConfigSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as BlacklistFilterStepConfig;
    const count = config.categoryIds?.length || 0;
    return count > 0 ? `${count} ${count === 1 ? 'category' : 'categories'} blacklisted` : 'No blacklist';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as BlacklistFilterStepConfig;
    const count = config.categoryIds?.length || 0;
    return `${count} blacklisted`;
  }
}
