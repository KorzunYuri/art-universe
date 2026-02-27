import { useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchPerson } from '../api/art-data-persons.ts';
import { artEntitiesKeys } from '../utils/query-keys.ts';

/**
 * Hook for fetching and managing a single person
 */
export function usePerson(personId: number) {
    const queryClient = useQueryClient();
    const queryKey = artEntitiesKeys.personDetail(personId);

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: artEntitiesKeys.all });
    };

    const personQuery = useQuery({
        queryKey,
        queryFn: () => fetchPerson(personId),
    });

    return {
        person: personQuery.data,
        invalidatePerson: invalidate,
        isLoading: personQuery.isLoading,
        isError: personQuery.isError,
        error: personQuery.error,
    };
}
