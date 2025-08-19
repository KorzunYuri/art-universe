import { useQuery, useQueryClient } from "@tanstack/react-query";
import { entityLookupKeys, rawEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import type { DataSource } from "@/music-universe/sources/shared/types/data-sources.ts";
import { fetchBoundMasterEntities } from "@/music-universe/music-data/api/music-data-common-binding.ts";
import { lookupMasterEntitiesWithParams } from "@/music-universe/music-data/api/music-data-common-lookup.ts";
import { LookupRequestSourceParams } from "@/music-universe/music-data/types/master-entities-lookup.ts";
import type { MasterEntityType, RawEntity } from "@/music-universe/shared/types/entities.ts";
import {RawEntityLookupContextFactory} from "@/music-universe/sources/shared/types/lookup-context.ts";

/**
 * Generic hook for fetching and managing raw entities from any data source
 * 
 * @param dataSource The data source identifier (e.g., 'lastfm')
 * @param entityType The type of entity to fetch (e.g., 'artist', 'track', 'category')
 * @param rawEntityId The ID of the raw entity to fetch
 * @param fetchFn Function that fetches the raw entity from the data source
 * @returns Object with entity data and utility functions
 */
export function useRawEntity<M extends MasterEntityType, R extends RawEntity<M>>(
    dataSource: DataSource,
    entityType: M,
    rawEntityId: number,
    fetchFn: () => Promise<R>
) {
    const queryClient = useQueryClient();
    const rawEntityQueryKey = rawEntitiesKeys.detail(dataSource, entityType, rawEntityId);

    /**
     * Updates the entity in the query cache
     */
    const update = (updatedEntity: RawEntity<M>) => {
        queryClient.setQueryData(rawEntityQueryKey, updatedEntity);
    }

    /**
     * Invalidates the entity in the query cache, forcing a refetch
     */
    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: rawEntityQueryKey });
    }

    const rawEntityQuery = useQuery({
        queryKey: rawEntityQueryKey,
        queryFn: async () => {
            // Fetch the raw entity using the provided fetch function
            const rawEntity = await fetchFn();

            try {
                // Assign master entity if bound
                const masterEntityBinding = await fetchBoundMasterEntities<M>(dataSource, entityType, [rawEntityId]);
                const masterEntity = masterEntityBinding?.length
                    ? masterEntityBinding[0].masterEntity
                    : undefined;
                rawEntity.setMasterEntity(masterEntity);
            } catch (error) {
                console.error(`Failed to fetch bound master entities: ${error}`);
                throw error;
            }

            try {
                // Update the cache for master entities lookup
                const searchSourceParams = new LookupRequestSourceParams(rawEntity.name, rawEntity);
                const masterEntityLookup = await lookupMasterEntitiesWithParams(entityType, searchSourceParams);
                const lookupParams = {
                    search: rawEntity.name,
                    context: RawEntityLookupContextFactory.fromRawEntity(rawEntity)
                };
                const masterEntityLookupQueryKey = entityLookupKeys.query('master', entityType, lookupParams);
                queryClient.setQueryData(masterEntityLookupQueryKey, masterEntityLookup);
            } catch (error) {
                console.error(`Failed to update master entities lookup: ${error}`);
                // Don't throw here as this is a non-critical operation
            }

            return rawEntity;
        }
    });

    return {
        entity:             rawEntityQuery.data,
        updateEntity:       update,
        invalidateEntity:   invalidate,
        isLoading:          rawEntityQuery.isLoading,
        isError:            rawEntityQuery.isError,
        error:              rawEntityQuery.error,
    };
}
