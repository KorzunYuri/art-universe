import { useQuery } from "@tanstack/react-query";
import { LookupRegistry } from "@/music-universe/shared/services/LookupRegistry";
import type { MasterEntityType } from "@/music-universe/shared/types/entities";
import type { DataSource } from "@/music-universe/sources/shared/types/data-sources";
import type { LookupEntity, BaseLookupParams } from "@/music-universe/shared/types/lookup";
import {entityLookupKeys} from "@/music-universe/shared/utils/query-keys.ts";

/**
 * Universal lookup hook that works with any data source and entity type
 * Uses the LookupRegistry to find the appropriate lookup configuration
 *
 * @param dataSource The data source ('master', 'lastfm', etc.)
 * @param entityType The entity type ('artist', 'track', etc.)
 * @param params Lookup parameters (search string and additional context)
 * @param enableEmptySearch
 * @returns Object with lookup results and loading state
 */
export function useEntityLookup<TParams extends BaseLookupParams>(
    dataSource: DataSource | 'master',
    entityType: MasterEntityType,
    params: TParams,
    enableEmptySearch: boolean = false
) {
    const queryKey = entityLookupKeys.query(dataSource, entityType, params);

    const lookupQuery = useQuery({
        queryKey,
        queryFn: async (): Promise<LookupEntity[]> => {
            if (!params.search.trim() && !enableEmptySearch) {
                return [];
            }

            return await LookupRegistry.lookup(dataSource, entityType, params);
        },
        staleTime: 5 * 60 * 1000, // 5 minutes
    });

    return {
        currentOptions: lookupQuery.data ?? [],
        isLoading: lookupQuery.isLoading,
        isError: lookupQuery.isError,
        error: lookupQuery.error,
    };
}
