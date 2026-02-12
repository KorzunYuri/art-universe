import type { SortingState } from '@tanstack/react-table';

/**
 * Converts TanStack Table SortingState to backend sort string format ("field,dir").
 * Only uses the first sort column (single-column sorting).
 */
export function sortingStateToString(sorting: SortingState): string {
    if (sorting.length === 0) return '';
    const { id, desc } = sorting[0];
    return `${id},${desc ? 'desc' : 'asc'}`;
}

/**
 * Converts backend sort string format ("field,dir") to TanStack Table SortingState.
 */
export function stringToSortingState(sort: string): SortingState {
    if (!sort) return [];
    const [field, dir] = sort.split(',');
    if (!field) return [];
    return [{ id: field, desc: dir === 'desc' }];
}
