import { stepRegistry } from '../steps/StepRegistry';

// Pipeline step types based on backend StepType enum
export type PipelineStepType = 
  | 'APPROVED_FILTER'
  | 'BLACKLIST_FILTER' 
  | 'WHITELIST_FILTER'
  | 'TRACK_RECENCY_PENALTY'
  | 'ARTIST_RECENCY_PENALTY'
  | 'ARTIST_DIVERSITY'
  | 'FINAL_LIMITER'
  | 'FINAL_CATEGORIES_BALANCER'
  | 'START_DATASOURCE';

export type StepPosition = 'INITIAL' | 'TRANSFORM';

// Step configuration interfaces matching backend DTOs
export interface CategoryWeight {
  id: number;
  weight: number;
}

export interface StartDatasourceStepConfig {
  type: 'START_DATASOURCE';
  datasource?: string;
}

export interface BlacklistFilterStepConfig {
  type: 'BLACKLIST_FILTER';
  categoryIds: number[];
}

export interface WhitelistFilterStepConfig {
  type: 'WHITELIST_FILTER';
  categories: CategoryWeight[];
}

export interface FinalLimiterStepConfig {
  type: 'FINAL_LIMITER';
  targetCount: number;
}

export interface FinalCategoriesBalancerConfig {
  type: 'FINAL_CATEGORIES_BALANCER';
  targetCount: number;
  defaultQuota: number;
  categories: CategoryWeight[];
}

// Union type for all step configurations
export type StepConfig = 
  | StartDatasourceStepConfig
  | BlacklistFilterStepConfig
  | WhitelistFilterStepConfig
  | FinalLimiterStepConfig
  | FinalCategoriesBalancerConfig
  | { type: Exclude<PipelineStepType, 'START_DATASOURCE' | 'BLACKLIST_FILTER' | 'WHITELIST_FILTER' | 'FINAL_LIMITER' | 'FINAL_CATEGORIES_BALANCER'> };

// Pipeline DTO types
export interface PipelineStepDto {
  id?: number;
  type: PipelineStepType;
  algVersion?: number;
  cfgData?: string;
  previewData?: string;
  ord: number;
  resultTableName?: string;
  resultStats?: string;
}

export interface PipelineDto {
  id: number;
  immutable: boolean;
  steps: PipelineStepDto[];
}

export interface GameWithPipelineDto {
  id: number;
  createdAt: string;
  pipeline: PipelineDto;
}

export interface GameWithPipelineIdDto {
  id: number;
  createdAt: string;
  pipelineId: number;
}

// Utility functions
export function parseStepConfig(type: PipelineStepType, cfgData?: string): StepConfig {
  if (!cfgData) {
    return { type } as StepConfig;
  }
  
  try {
    const parsed = JSON.parse(cfgData);
    return { type, ...parsed } as StepConfig;
  } catch {
    return { type } as StepConfig;
  }
}

export function serializeStepConfig(config: StepConfig): string {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { type, ...rest } = config;
  return JSON.stringify(rest);
}

export function validatePipeline(steps: PipelineStepDto[]): { isValid: boolean; errors: string[]; warnings: string[] } {
  const errors: string[] = [];
  const warnings: string[] = [];

  if (steps.length === 0) {
    errors.push('Pipeline must have at least one step');
    return { isValid: false, errors, warnings };
  }

  const sortedSteps = [...steps].sort((a, b) => a.ord - b.ord);
  const positions = sortedSteps.map(step => stepRegistry.get(step.type).getPosition());

  // Check for INITIAL step at position 0
  if (positions[0] !== 'INITIAL') {
    errors.push('Pipeline must start with an INITIAL step (START_DATASOURCE)');
  }

  // Check for exactly one INITIAL step
  const initialCount = positions.filter(p => p === 'INITIAL').length;

  if (initialCount === 0) {
    errors.push('Pipeline must have exactly one INITIAL step');
  } else if (initialCount > 1) {
    errors.push('Pipeline can have only one INITIAL step');
  }

  // Check for INITIAL steps in positions > 0 (warning, not error)
  positions.forEach((pos, index) => {
    if (pos === 'INITIAL' && index > 0) {
      warnings.push('INITIAL step should be at position 0');
    }
  });

  return { isValid: errors.length === 0, errors, warnings };
}
