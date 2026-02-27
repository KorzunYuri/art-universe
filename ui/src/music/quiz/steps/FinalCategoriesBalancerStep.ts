import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto, FinalCategoriesBalancerConfig } from '../types/pipeline-steps';
import { parseStepConfig } from '../types/pipeline-steps';

export class FinalCategoriesBalancerStep extends BaseStep {
  getType(): PipelineStepType {
    return 'FINAL_CATEGORIES_BALANCER';
  }

  getLabel(): string {
    return 'Final Categories Balancer';
  }

  getDescription(): string {
    return 'Balances the final track list to achieve a desired distribution across specified categories.';
  }

  getPosition(): StepPosition {
    return 'TRANSFORM';
  }

  isConfigurable(): boolean {
    return true;
  }

  getDefaultConfig(): StepConfig {
    return {
      type: 'FINAL_CATEGORIES_BALANCER',
      targetCount: 10,
      defaultQuota: 0.5,
      categories: []
    };
  }

  getConfigSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as FinalCategoriesBalancerConfig;
    const categoryCount = config.categories?.length || 0;
    const targetCount = config.targetCount || 10;
    return categoryCount > 0
      ? `${categoryCount} ${categoryCount === 1 ? 'category' : 'categories'} balanced, target: ${targetCount} tracks`
      : `Categories balanced, target: ${targetCount} tracks`;
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as FinalCategoriesBalancerConfig;
    return `${config.categories?.length || 0} balanced`;
  }
}
