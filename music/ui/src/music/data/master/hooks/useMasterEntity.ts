import {useQuery, useQueryClient} from "@tanstack/react-query";
import {masterEntitiesKeys} from "@/music/shared/utils/query-keys.ts";
import type {MasterEntityMap, MasterEntityType} from "@/music/shared/types/entities.ts";
import {fetchMasterEntity} from "@/music/data/master/api/music-data-common-fetching.ts";

/**
 * Hook for fetching and managing master entities
 * 
 * @param entityType The type of entity to fetch (e.g., 'artist', 'track', 'category')
 * @param entityId The ID of the master entity to fetch
 * @returns Object with entity data and utility functions
 */
export function useMasterEntity<T extends MasterEntityType>(
    entityType: T,
    entityId: number
) {
    const queryClient = useQueryClient();
    const masterEntityQueryKey = masterEntitiesKeys.detail(entityType, entityId);

    /**
     * Updates the entity in the query cache
     */
    const update = (updatedEntity: MasterEntityMap[T]) => {
        queryClient.setQueryData(masterEntityQueryKey, updatedEntity);
    }

    /**
     * Invalidates the entity in the query cache, forcing a refetch
     */
    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: masterEntityQueryKey });
    }

    const masterEntityQuery = useQuery({
        queryKey: masterEntityQueryKey,
        queryFn: async () => {
            console.log(`🔄 Fetching ${entityType} master entity with ID: ${entityId}`);
            return await fetchMasterEntity(entityType, entityId);
        }
    });

    return {
        entity:             masterEntityQuery.data,
        updateEntity:       update,
        invalidateEntity:   invalidate,
        isLoading:          masterEntityQuery.isLoading,
        isError:            masterEntityQuery.isError,
        error:              masterEntityQuery.error,
    };
}
