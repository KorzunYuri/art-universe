import { BaseStep } from './Step';
import type { PipelineStepType, StepConfig, StepPosition, PipelineStepDto, StartDatasourceStepConfig } from '../types/pipeline-steps';
import { parseStepConfig } from '../types/pipeline-steps';

export class StartDatasourceStep extends BaseStep {
  getType(): PipelineStepType {
    return 'START_DATASOURCE';
  }

  getLabel(): string {
    return 'Start Datasource';
  }

  getDescription(): string {
    return 'Loads tracks from a specified data source as the initial input for the pipeline.';
  }

  getPosition(): StepPosition {
    return 'INITIAL';
  }

  isConfigurable(): boolean {
    return false;
  }

  getDefaultConfig(): StepConfig {
    return { type: 'START_DATASOURCE' };
  }

  getConfigSummary(step: PipelineStepDto): string {
    const config = parseStepConfig(step.type, step.cfgData) as StartDatasourceStepConfig;
    return config.datasource ? `Datasource: ${config.datasource}` : 'No datasource configured';
  }

  getConfigShortSummary(step: PipelineStepDto): string {
    return 'Datasource';
  }
}
