import { useState, useCallback, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { masterEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import type { MasterEntityType } from "@/music-universe/shared/types/entities.ts";
import type { MasterEntityPageSearchParamsMap } from "@/music-universe/music-data/api/music-data-common-fetching.ts";
import { fetchMasterEntities } from "@/music-universe/music-data/api/music-data-common-fetching.ts";

/**
 * Hook for managing master entity tables
 * 
 * @param entityType The type of entity to fetch (e.g., 'artist', 'track', 'category')
 * @param initialParams Initial search parameters
 * @returns Object with data and functions to control the table
 */
export function useMasterEntityTable<T extends MasterEntityType>(
    entityType: T,
    initialParams: Partial<MasterEntityPageSearchParamsMap[T]> = {}
) {

    // Memoize initial params to prevent recreation
    const memoizedInitialParams = useMemo(() => initialParams, [JSON.stringify(initialParams)]);
    
    // State for table data and UI
    const [params, setParams] = useState<MasterEntityPageSearchParamsMap[T]>(() => ({
        page: 0,
        size: 20,
        search: '',
        sort: 'name,asc',
        ...memoizedInitialParams,
    }));
    
    // Query key for this entity list
    const masterEntitiesQueryKey = useMemo(() => {
        console.log(`🔑 Creating query key for ${entityType}:`, params);
        return masterEntitiesKeys.list(entityType, params);
    }, [entityType, params]);

    const masterEntitiesPageQuery = useQuery({
        queryKey: masterEntitiesQueryKey,
        queryFn: async () => {
            console.log(`🔄 Fetching ${entityType} entities with params:`, params);
            return await fetchMasterEntities(entityType, params);
        }
    });

    const setSearch = useCallback((search: string) => setParams(
        prev => (
            {
                ...prev,
                search,
                page: 0
            }
        )), []);

    const setSort = useCallback((sort: string) => setParams(
        prev => (
            {
                ...prev,
                sort
            }
        )), []);

    const nextPage = useCallback(() => {
        if (masterEntitiesPageQuery.data && params.page < masterEntitiesPageQuery.data.totalPages - 1) {
            setParams(
                prev => (
                    {
                        ...prev,
                        page: prev.page + 1
                    }
                ));
        }
    }, [masterEntitiesPageQuery.data?.totalPages, params.page]);
    
    const prevPage = useCallback(() => {
        if (params.page > 0) {
            setParams(
                prev => (
                    {
                        ...prev,
                        page: prev.page - 1
                    }
                ));
        }
    }, [params.page]);

    const goToPage = useCallback((page: number) => {
        if (page >= 0 && masterEntitiesPageQuery.data && page < masterEntitiesPageQuery.data.totalPages) {
            setParams(prev => ({ ...prev, page }));
        }
    }, [masterEntitiesPageQuery.data?.totalPages]);

    const refresh = useCallback(() => {
        masterEntitiesPageQuery.refetch();
    }, [masterEntitiesPageQuery.refetch]);

    const entityIds = useMemo(() => 
        masterEntitiesPageQuery.data?.content.map(e => e.id), 
        [masterEntitiesPageQuery.data?.content]
    );

    const entities = useMemo(() => 
        masterEntitiesPageQuery.data?.content, 
        [masterEntitiesPageQuery.data?.content]
    );

    const pagination = useMemo(() => ({
        page: params.page,
        totalPages: masterEntitiesPageQuery.data?.totalPages || 0,
        hasNextPage: masterEntitiesPageQuery.data ? params.page < masterEntitiesPageQuery.data.totalPages - 1 : false,
        hasPrevPage: params.page > 0,
    }), [params.page, masterEntitiesPageQuery.data?.totalPages]);

    return {
        entityIds,
        entities,
        pagination,
        search: params.search || '',
        sort: params.sort || 'name,asc',
        isLoading: masterEntitiesPageQuery.isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        refresh,
    };
}
