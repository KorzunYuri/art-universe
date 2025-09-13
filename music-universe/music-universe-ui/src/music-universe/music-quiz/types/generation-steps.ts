export interface CategoryWeight {
    id: number;
    name: string;
    weight: number; // 0.0 - 1.0
}

export interface GenerationStepUI {
    id: string; // temporary UI ID
    type: 'WHITELIST_FILTER';
    categories: CategoryWeight[];
}

export type StepType = 'WHITELIST_FILTER';
