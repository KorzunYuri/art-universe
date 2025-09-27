import { StepRegistry } from '@/music/quiz/types/step-registry.ts';
import { BlacklistFilterStep } from './BlacklistFilterStep.tsx';
import { WhitelistFilterStep } from './WhitelistFilterStep.tsx';
import { ArtistRecencyPenaltyStep } from './ArtistRecencyPenaltyStep.tsx';
import { ArtistDiversityStep } from './ArtistDiversityStep.tsx';
import { FinalSelectionStep } from './FinalSelectionStep.tsx';
import { FinalCategoriesBalancerStep } from './FinalCategoriesBalancerStep.tsx';

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
  type: 'ARTIST_RECENCY_PENALTY',
  label: 'Artist Recency Penalty',
  isFinal: false,
  createDefault: () => ({
    id: `artist-recency-penalty-${Date.now()}`,
    type: 'ARTIST_RECENCY_PENALTY'
  }),
  component: ArtistRecencyPenaltyStep
});

StepRegistry.register({
  type: 'ARTIST_DIVERSITY',
  label: 'Artist Diversity',
  isFinal: false,
  createDefault: () => ({
    id: `artist-diversity-${Date.now()}`,
    type: 'ARTIST_DIVERSITY'
  }),
  component: ArtistDiversityStep
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
    defaultQuota: 0.5,
    targetCount: 10
  }),
  component: FinalCategoriesBalancerStep
});

export * from './BlacklistFilterStep.tsx';
export * from './WhitelistFilterStep.tsx';
export * from './ArtistRecencyPenaltyStep.tsx';
export * from './ArtistDiversityStep.tsx';
export * from './FinalSelectionStep.tsx';
export * from './FinalCategoriesBalancerStep.tsx';
