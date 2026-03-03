import type { PipelineStepType } from '@/music/quiz/types/pipeline-steps.ts';
import { FinalLimiterConfig } from './FinalLimiterConfig.tsx';
import { BlacklistFilterConfig } from './BlacklistFilterConfig.tsx';
import { WhitelistFilterConfig } from './WhitelistFilterConfig.tsx';
import { FinalCategoriesBalancerConfig } from './FinalCategoriesBalancerConfig.tsx';
import type { PipelineStepDto } from '@/music/quiz/types/pipeline-steps.ts';

/**
 * Props interface for step configuration components
 */
export interface StepConfigProps {
  step: PipelineStepDto;
  onUpdate: (updatedStep: PipelineStepDto) => void;
  readonly?: boolean;
}

/**
 * Registry mapping step types to their React configuration components.
 * Only configurable steps have entries here.
 */
const STEP_CONFIG_COMPONENTS: Partial<Record<PipelineStepType, React.ComponentType<StepConfigProps>>> = {
  FINAL_LIMITER: FinalLimiterConfig,
  BLACKLIST_FILTER: BlacklistFilterConfig,
  WHITELIST_FILTER: WhitelistFilterConfig,
  FINAL_CATEGORIES_BALANCER: FinalCategoriesBalancerConfig,
};

/**
 * Gets the React configuration component for a step type.
 * Returns null for config-free steps (no configuration UI needed).
 */
export function getStepConfigComponent(type: PipelineStepType): React.ComponentType<StepConfigProps> | null {
  return STEP_CONFIG_COMPONENTS[type] || null;
}
