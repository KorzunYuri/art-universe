import { StepRegistry } from '../../types/step-registry';
import { BlacklistFilterStep } from './BlacklistFilterStep';
import { WhitelistFilterStep } from './WhitelistFilterStep';
import { FinalSelectionStep } from './FinalSelectionStep';
import { FinalCategoriesBalancerStep } from './FinalCategoriesBalancerStep';

// Register all step types
StepRegistry.register({
  type: 'BLACKLIST_FILTER',
  label: 'Blacklist Filter',
  isFinal: false,
  createDefault: () => ({
    id: `blacklist-filter-${Date.now()}`,
    type: 'BLACKLIST_FILTER',
    categoryIds: [],
    blacklistCategories: []
  }),
  component: BlacklistFilterStep
});

StepRegistry.register({
  type: 'WHITELIST_FILTER',
  label: 'Whitelist Filter',
  isFinal: false,
  createDefault: () => ({
    id: `whitelist-filter-${Date.now()}`,
    type: 'WHITELIST_FILTER',
    categories: []
  }),
  component: WhitelistFilterStep
});

StepRegistry.register({
  type: 'FINAL_SELECTION',
  label: 'Final Selection',
  isFinal: true,
  createDefault: () => ({
    id: `final-selection-${Date.now()}`,
    type: 'FINAL_SELECTION',
    targetCount: 10
  }),
  component: FinalSelectionStep
});

StepRegistry.register({
  type: 'FINAL_CATEGORIES_BALANCER',
  label: 'Final Categories Balancer',
  isFinal: true,
  createDefault: () => ({
    id: `final-categories-balancer-${Date.now()}`,
    type: 'FINAL_CATEGORIES_BALANCER',
    categories: [],
    targetCount: 10
  }),
  component: FinalCategoriesBalancerStep
});

export * from './BlacklistFilterStep';
export * from './WhitelistFilterStep';
export * from './FinalSelectionStep';
export * from './FinalCategoriesBalancerStep';
