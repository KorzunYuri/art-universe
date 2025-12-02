import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto, WhitelistFilterStepConfig } from '../types/pipeline-steps';
import { parseStepConfig } from '../types/pipeline-steps';

export class WhitelistFilterStep extends BaseStep {
  getType(): PipelineStepType {
    return 'WHITELIST_FILTER';
  }

  getLabel(): string {
    return 'Whitelist Filter';
  }

  getDescription(): string {
    return 'Keeps only tracks belonging to specific categories, removing all others.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return true;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'WHITELIST_FILTER', categories: [] };
  }

  getConfigSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as WhitelistFilterStepConfig;
    const count = config.categories?.length || 0;
    return count > 0 ? `${count} ${count === 1 ? 'category' : 'categories'} whitelisted` : 'No whitelist';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as WhitelistFilterStepConfig;
    const count = config.categories?.length || 0;
    return `${count} whitelisted`;
  }
}
