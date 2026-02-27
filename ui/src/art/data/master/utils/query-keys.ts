import type { PersonPageSearchParams } from '../api/art-data-persons.ts';

/**
 * Query keys for art entities
 */
export const artEntitiesKeys = {
    all: ['artEntities'] as const,
    persons: (params?: PersonPageSearchParams) =>
        [...artEntitiesKeys.all, 'persons', params] as const,
    personDetail: (id: number) =>
        [...artEntitiesKeys.all, 'persons', 'detail', id] as const,
};
