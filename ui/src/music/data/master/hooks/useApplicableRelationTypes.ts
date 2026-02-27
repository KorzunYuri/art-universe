import { useQuery } from '@tanstack/react-query';
import { fetchApplicableRelationTypes } from '@/music/data/master/api/music-data-relation-types';
import { relationTypeKeys } from '@/music/shared/utils/query-keys';
import type { MasterEntityType } from '@/music/shared/types/entities';

/**
 * Hook to fetch relation types applicable to a specific source→target entity type pair.
 * Uses a long staleTime since relation type definitions rarely change.
 */
export function useApplicableRelationTypes(
    sourceEntityType: MasterEntityType,
    targetEntityType: MasterEntityType
) {
    const { data: relationTypes, isLoading, isError } = useQuery({
        queryKey: relationTypeKeys.applicable(sourceEntityType, targetEntityType),
        queryFn: () => fetchApplicableRelationTypes(sourceEntityType, targetEntityType),
        staleTime: 10 * 60 * 1000, // 10 minutes
    });

    return { relationTypes: relationTypes ?? [], isLoading, isError };
}
