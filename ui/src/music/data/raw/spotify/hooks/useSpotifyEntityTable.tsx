import {useState, useCallback, useEffect} from "react";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {entityLookupKeys, rawEntitiesKeys, ENTITY_LOOKUP_LIMIT} from "@/music/shared/utils/query-keys.ts";
import type {DataSource} from "@/music/data/raw/shared/types/data-sources.ts";
import type {SpotifySupportedEntityType, SpotifySupportedEntityTypeMap} from "@/music/data/raw/spotify/types/spotify-entity.ts";
import {
    fetchSpotifyEntities,
    type SpotifyPageSearchParamsMap
} from "@/music/data/raw/spotify/api/spotify-common-fetching.ts";
import {LookupRequestSourceParams} from "@/music/data/master/types/master-entities-lookup.ts";
import {fetchBoundMasterEntities} from "@/music/data/master/api/music-data-common-binding.ts";
import {batchLookupMasterEntitiesWithParams} from "@/music/data/master/api/music-data-common-lookup.ts";
import {RawEntityLookupContextFactory} from "@/music/data/raw/shared/types/lookup-context.ts";
import type {Page} from "@/shared/types/page.ts";

function getDefaultSort(entityType: SpotifySupportedEntityType): string {
    return 'name,asc';
}

interface PageLoadResult<T extends SpotifySupportedEntityType> {
    page: Page<SpotifySupportedEntityTypeMap[T]>;
    rawEntityIds: number[];
}

export function useSpotifyEntityTable<T extends SpotifySupportedEntityType>(
    entityType: T,
    initialParams: Partial<SpotifyPageSearchParamsMap[T]> = {}
) {
    const queryClient = useQueryClient();
    const [localSearch, setLocalSearch] = useState(initialParams.search || '');
    const [params, setParams] = useState<SpotifyPageSearchParamsMap[T]>({
        page: 0,
        size: 20,
        search: '',
        sort: getDefaultSort(entityType),
        ...initialParams,
    });

    const dataSource: DataSource = 'spotify';

    const loadPageWithRelatedData = useCallback(async (
        pageParams: SpotifyPageSearchParamsMap[T]
    ): Promise<PageLoadResult<T>> => {
        // 1. Get raw entities from Spotify API
        const rawEntitiesPage = await fetchSpotifyEntities<T>(entityType, pageParams);
        const rawEntities = rawEntitiesPage.content;
        const rawEntityIds = rawEntities.map((entity) => entity.id);

        // 2. Load and assign master entities
        try {
            const masterEntityBindings = await fetchBoundMasterEntities(dataSource, entityType, rawEntityIds);
            rawEntities.forEach((entity) => {
                const boundEntityInfo = masterEntityBindings.find((e) => e.externalId === entity.id);
                // @ts-expect-error TS2345: Argument of type A | B is not assignable to type A & B
                entity.setMasterEntity(boundEntityInfo?.masterEntity);
                queryClient.setQueryData(rawEntitiesKeys.detail(dataSource, entityType, entity.id), entity);
            });
        } catch(error) {
            console.error(`Failed to batch-fetch bound master entities: ${error}`);
            throw error;
        }

        // 3. Update master entities lookup cache
        try {
            const request = rawEntities.map(entity => new LookupRequestSourceParams(entity.name, entity));
            const masterEntitiesLookups = await batchLookupMasterEntitiesWithParams(entityType, request, ENTITY_LOOKUP_LIMIT);
            rawEntities.forEach(rawEntity => {
                const lookupEntities = masterEntitiesLookups.results[rawEntity.name] ?? [];
                const lookupParams = {
                    search: rawEntity.name,
                    context: RawEntityLookupContextFactory.fromRawEntity(rawEntity),
                    limit: ENTITY_LOOKUP_LIMIT
                };
                queryClient.setQueryData(entityLookupKeys.query('master', entityType, lookupParams), lookupEntities);
            });
        } catch (error) {
            console.log(`Failed to batch update master entities lookup: ${error}`);
        }

        return {
            page: { ...rawEntitiesPage, content: rawEntities },
            rawEntityIds: rawEntityIds,
        };
    }, [entityType, dataSource, queryClient]);

    const rawEntitiesPageQueryKey = rawEntitiesKeys.list(dataSource, entityType, params);
    const rawEntitiesPageQuery = useQuery({
        queryKey: rawEntitiesPageQueryKey,
        queryFn: () => loadPageWithRelatedData(params)
    });

    const prefetchNextPage = useCallback(async () => {
        if (rawEntitiesPageQuery.data && params.page < rawEntitiesPageQuery.data.page.totalPages - 1) {
            const nextPageParams = { ...params, page: params.page + 1 };
            const nextPageQueryKey = rawEntitiesKeys.list(dataSource, entityType, nextPageParams);

            const existingData = queryClient.getQueryData(nextPageQueryKey);
            if (!existingData) {
                try {
                    const nextPageData = await loadPageWithRelatedData(nextPageParams);
                    queryClient.setQueryData(nextPageQueryKey, nextPageData);
                } catch (error) {
                    console.log(`Failed to prefetch next page: ${error}`);
                }
            }
        }
    }, [rawEntitiesPageQuery.data, params, dataSource, entityType, queryClient, loadPageWithRelatedData]);

    useEffect(() => {
        if (rawEntitiesPageQuery.isSuccess && !rawEntitiesPageQuery.isFetching) {
            const timeoutId = setTimeout(prefetchNextPage, 100);
            return () => clearTimeout(timeoutId);
        }
    }, [rawEntitiesPageQuery.isSuccess, rawEntitiesPageQuery.isFetching, prefetchNextPage]);

    const setSearch = (search: string) => {
        setLocalSearch(search);
    };

    const handleSearchSubmit = () => {
        setParams(prev => ({
            ...prev,
            search: localSearch,
            page: 0
        }));
    };

    const setSort = (sort: string) => setParams(prev => ({ ...prev, sort }));

    const nextPage = () => {
        if (rawEntitiesPageQuery.data && params.page < rawEntitiesPageQuery.data.page.totalPages - 1) {
            setParams(prev => ({ ...prev, page: prev.page + 1 }));
        }
    };
    const prevPage = () => {
        if (params.page > 0) {
            setParams(prev => ({ ...prev, page: prev.page - 1 }));
        }
    };

    const goToPage = (page: number) => {
        if (page >= 0 && rawEntitiesPageQuery.data && page < rawEntitiesPageQuery.data.page.totalPages) {
            setParams(prev => ({ ...prev, page }));
        }
    };

    const updateParams = (updates: Partial<SpotifyPageSearchParamsMap[T]>) => {
        setParams(prev => ({
            ...prev,
            ...updates,
            page: 0
        }));
    };

    const refresh = () => {
        rawEntitiesPageQuery.refetch();
    };

    return {
        rawEntityIds: rawEntitiesPageQuery.data?.rawEntityIds,
        rawEntities: rawEntitiesPageQuery.data?.page?.content,
        pagination: {
            page: params.page,
            totalPages: rawEntitiesPageQuery.data?.page?.totalPages || 0,
            hasNextPage: rawEntitiesPageQuery.data ? params.page < rawEntitiesPageQuery.data.page.totalPages - 1 : false,
            hasPrevPage: params.page > 0,
        },
        search: localSearch,
        sort: params.sort || getDefaultSort(entityType),
        isLoading: rawEntitiesPageQuery.isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        handleSearchSubmit,
        updateParams,
        refresh,
    };
}
