import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
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
    const queryClient = useQueryClient();
    
    // State for table data and UI
    const [params, setParams] = useState<MasterEntityPageSearchParamsMap[T]>({
        page: 0,
        size: 20,
        search: '',
        sort: 'name,asc',
        ...initialParams,
    });
    
    // Query key for this entity list
    const masterEntitiesQueryKey = masterEntitiesKeys.list(entityType, params);

    const masterEntitiesPageQuery = useQuery({
        queryKey: masterEntitiesQueryKey,
        queryFn: async () => {
            console.log(`🔄 Loading ${entityType} master entities with params:`, params);

            // Fetch master entities
            const entitiesPage = await fetchMasterEntities(entityType, params);

            // Cache individual entities for detail views
            entitiesPage.content.forEach(entity => {
                queryClient.setQueryData(
                    masterEntitiesKeys.detail(entityType, entity.id),
                    entity
                );
            });

            return entitiesPage;
        }
    });

    const setSearch = (search: string) => setParams(
        prev => (
            {
                ...prev,
                search,
                page: 0
            }
        ));

    const setSort = (sort: string) => setParams(
        prev => (
            {
                ...prev,
                sort
            }
        ));

    const nextPage = () => {
        if (masterEntitiesPageQuery.data && params.page < masterEntitiesPageQuery.data.totalPages - 1) {
            setParams(
                prev => (
                    {
                        ...prev,
                        page: prev.page + 1
                    }
                ));
        }
    };
    const prevPage = () => {
        if (params.page > 0) {
            setParams(
                prev => (
                    {
                        ...prev,
                        page: prev.page - 1
                    }
                ));
        }
    };

    const goToPage = (page: number) => {
        if (page >= 0 && masterEntitiesPageQuery.data && page < masterEntitiesPageQuery.data.totalPages) {
            setParams(prev => ({ ...prev, page }));
        }
    };

    const refresh = () => {
        masterEntitiesPageQuery.refetch();
    };

    return {
        entityIds: masterEntitiesPageQuery.data?.content.map(e => e.id),
        entities: masterEntitiesPageQuery.data?.content,
        pagination: {
            page: params.page,
            totalPages: masterEntitiesPageQuery.data?.totalPages || 0,
            hasNextPage: masterEntitiesPageQuery.data ? params.page < masterEntitiesPageQuery.data.totalPages - 1 : false,
            hasPrevPage: params.page > 0,
        },
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
