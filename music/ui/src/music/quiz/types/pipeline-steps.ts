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

export type StepPosition = 'START' | 'MIDDLE' | 'FINAL';

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

// Step position mapping
export const STEP_POSITIONS: Record<PipelineStepType, StepPosition> = {
  'START_DATASOURCE': 'START',
  'APPROVED_FILTER': 'MIDDLE',
  'BLACKLIST_FILTER': 'MIDDLE',
  'WHITELIST_FILTER': 'MIDDLE',
  'TRACK_RECENCY_PENALTY': 'MIDDLE',
  'ARTIST_RECENCY_PENALTY': 'MIDDLE',
  'ARTIST_DIVERSITY': 'MIDDLE',
  'FINAL_LIMITER': 'FINAL',
  'FINAL_CATEGORIES_BALANCER': 'FINAL'
};

// Step labels for UI
export const STEP_LABELS: Record<PipelineStepType, string> = {
  'START_DATASOURCE': 'Start Datasource',
  'APPROVED_FILTER': 'Approved Filter',
  'BLACKLIST_FILTER': 'Blacklist Filter',
  'WHITELIST_FILTER': 'Whitelist Filter',
  'TRACK_RECENCY_PENALTY': 'Track Recency Penalty',
  'ARTIST_RECENCY_PENALTY': 'Artist Recency Penalty',
  'ARTIST_DIVERSITY': 'Artist Diversity',
  'FINAL_LIMITER': 'Final Limiter',
  'FINAL_CATEGORIES_BALANCER': 'Final Categories Balancer'
};

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
  const { type, ...rest } = config;
  return JSON.stringify(rest);
}

export function validatePipeline(steps: PipelineStepDto[]): { isValid: boolean; errors: string[] } {
  const errors: string[] = [];
  
  if (steps.length === 0) {
    errors.push('Pipeline must have at least one step');
    return { isValid: false, errors };
  }
  
  const sortedSteps = [...steps].sort((a, b) => a.ord - b.ord);
  const positions = sortedSteps.map(step => STEP_POSITIONS[step.type]);
  
  // Check for START step
  if (positions[0] !== 'START') {
    errors.push('Pipeline must start with a START step');
  }
  
  // Check for FINAL step
  if (positions[positions.length - 1] !== 'FINAL') {
    errors.push('Pipeline must end with a FINAL step');
  }
  
  // Check for multiple START or FINAL steps
  const startCount = positions.filter(p => p === 'START').length;
  const finalCount = positions.filter(p => p === 'FINAL').length;
  
  if (startCount > 1) {
    errors.push('Pipeline can have only one START step');
  }
  
  if (finalCount > 1) {
    errors.push('Pipeline can have only one FINAL step');
  }
  
  return { isValid: errors.length === 0, errors };
}
