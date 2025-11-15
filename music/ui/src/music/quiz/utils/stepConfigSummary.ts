import {
  type PipelineStepDto,
  type StartDatasourceStepConfig,
  type BlacklistFilterStepConfig,
  type WhitelistFilterStepConfig,
  type FinalLimiterStepConfig,
  type FinalCategoriesBalancerConfig,
  parseStepConfig
} from '@/music/quiz/types/pipeline-steps.ts';

/**
 * Generates a human-readable summary of a step's configuration
 * for display in the pipeline stepper header
 */
export function getStepConfigSummary(step: PipelineStepDto): string {
  const config = parseStepConfig(step.type, step.cfgData);

  switch (step.type) {
    case 'START_DATASOURCE': {
      const cfg = config as StartDatasourceStepConfig;
      return cfg.datasource ? `Datasource: ${cfg.datasource}` : 'No datasource configured';
    }

    case 'APPROVED_FILTER':
      return 'Approved tracks only';

    case 'BLACKLIST_FILTER': {
      const cfg = config as BlacklistFilterStepConfig;
      const count = cfg.categoryIds?.length || 0;
      return count > 0 ? `${count} ${count === 1 ? 'category' : 'categories'} blacklisted` : 'No blacklist';
    }

    case 'WHITELIST_FILTER': {
      const cfg = config as WhitelistFilterStepConfig;
      const count = cfg.categories?.length || 0;
      return count > 0 ? `${count} ${count === 1 ? 'category' : 'categories'} whitelisted` : 'No whitelist';
    }

    case 'TRACK_RECENCY_PENALTY':
      return 'Track recency penalty applied';

    case 'ARTIST_RECENCY_PENALTY':
      return 'Artist recency penalty applied';

    case 'ARTIST_DIVERSITY':
      return 'Artist diversity applied';

    case 'FINAL_LIMITER': {
      const cfg = config as FinalLimiterStepConfig;
      const count = cfg.targetCount || 10;
      return `Limit to ${count} tracks`;
    }

    case 'FINAL_CATEGORIES_BALANCER': {
      const cfg = config as FinalCategoriesBalancerConfig;
      const categoryCount = cfg.categories?.length || 0;
      const targetCount = cfg.targetCount || 10;
      return categoryCount > 0
        ? `${categoryCount} ${categoryCount === 1 ? 'category' : 'categories'} balanced, target: ${targetCount} tracks`
        : `Categories balanced, target: ${targetCount} tracks`;
    }

    default:
      return 'Unknown configuration';
  }
}

/**
 * Returns a shortened version of the config summary for very limited space
 */
export function getStepConfigShortSummary(step: PipelineStepDto): string {
  const config = parseStepConfig(step.type, step.cfgData);

  switch (step.type) {
    case 'START_DATASOURCE':
      return 'Datasource';

    case 'APPROVED_FILTER':
      return 'Approved only';

    case 'BLACKLIST_FILTER': {
      const cfg = config as BlacklistFilterStepConfig;
      const count = cfg.categoryIds?.length || 0;
      return `${count} blacklisted`;
    }

    case 'WHITELIST_FILTER': {
      const cfg = config as WhitelistFilterStepConfig;
      const count = cfg.categories?.length || 0;
      return `${count} whitelisted`;
    }

    case 'TRACK_RECENCY_PENALTY':
      return 'Track recency';

    case 'ARTIST_RECENCY_PENALTY':
      return 'Artist recency';

    case 'ARTIST_DIVERSITY':
      return 'Artist diversity';

    case 'FINAL_LIMITER': {
      const cfg = config as FinalLimiterStepConfig;
      return `Limit ${cfg.targetCount || 10}`;
    }

    case 'FINAL_CATEGORIES_BALANCER': {
      const cfg = config as FinalCategoriesBalancerConfig;
      return `${cfg.categories?.length || 0} balanced`;
    }

    default:
      return 'Unknown';
  }
}
