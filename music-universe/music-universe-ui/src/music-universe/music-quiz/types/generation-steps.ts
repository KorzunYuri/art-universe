export interface CategoryWeight {
    id: number;
    name: string;
    weight: number; // 0.0 - 1.0
}

export interface GenerationStepUI {
    id: string; // temporary UI ID
    type: StepType;
    categories?: CategoryWeight[];
    categoryIds?: number[];
    blacklistCategories?: Array<{ id: number; name: string }>;
    defaultQuota?: number;
    targetCount?: number;
}

export type StepType = 'BLACKLIST_FILTER' | 'WHITELIST_FILTER' | 'FINAL_SELECTION' | 'FINAL_CATEGORIES_BALANCER';

export type IntermediateStepType = 'BLACKLIST_FILTER' | 'WHITELIST_FILTER';
export type FinalStepType = 'FINAL_SELECTION' | 'FINAL_CATEGORIES_BALANCER';

export const isFinalStep = (stepType: StepType): stepType is FinalStepType => {
    return stepType === 'FINAL_SELECTION' || stepType === 'FINAL_CATEGORIES_BALANCER';
};
