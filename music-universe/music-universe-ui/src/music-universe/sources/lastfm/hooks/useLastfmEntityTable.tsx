import {useState, useCallback, useEffect} from "react";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {entityLookupKeys, rawEntitiesKeys} from "@/music-universe/shared/utils/query-keys.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import type {LastfmSupportedEntityType} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";
import {
    fetchLastfmEntities,
    type LastfmPageSearchParamsMap
} from "@/music-universe/sources/lastfm/api/lastfm-common-fetching.ts";
import {LookupRequestSourceParams} from "@/music-universe/music-data/types/master-entities-lookup.ts";
import {fetchBoundMasterEntities} from "@/music-universe/music-data/api/music-data-common-binding.ts";
import {batchLookupMasterEntitiesWithParams} from "@/music-universe/music-data/api/music-data-common-lookup.ts";
import {getQuizBindings} from "@/music-universe/music-quiz/api/music-quiz-common-binding";
import {quizBindingKeys} from "@/music-universe/music-quiz/hooks/useQuizBinding";
import type {MusicQuizSupportedEntityType} from "@/music-universe/music-quiz/types/music-quiz-entity";
import {RawEntityLookupContextFactory} from "@/music-universe/sources/shared/types/lookup-context.ts";
import type {Page} from "@/music-universe/shared/types/page.ts";
import type {LastfmSupportedEntityTypeMap} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";

/**
 * Result of loading a page with all related data
 */
interface PageLoadResult<T extends LastfmSupportedEntityType> {
    page: Page<LastfmSupportedEntityTypeMap[T]>;
    rawEntityIds: number[];
}

export function useLastfmEntityTable<T extends LastfmSupportedEntityType>(
    entityType: T,
    initialParams: Partial<LastfmPageSearchParamsMap[T]> = {}
) {
    const queryClient = useQueryClient();
    const [params, setParams] = useState<LastfmPageSearchParamsMap[T]>({
        page: 0,
        size: 20,
        search: '',
        sort: 'name,asc',
        ...initialParams,
    });

    const dataSource: DataSource = 'lastfm';

    /**
     * Loads a page of entities with all related data (master entities, lookups, quiz bindings)
     */
    const loadPageWithRelatedData = useCallback(async (
        pageParams: LastfmPageSearchParamsMap[T]
    ): Promise<PageLoadResult<T>> => {
        console.log(`Loading ${entityType} page ${pageParams.page} with related data`);

        // 1. Get raw entities from LastFM API
        const rawEntitiesPage = await fetchLastfmEntities<T>(entityType, pageParams);
        const rawEntities = rawEntitiesPage.content;
        const rawEntityIds = rawEntities.map((entity) => entity.id);

        // 2. Load and assign master entities to raw entities
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
            // TODO fix the situation where there can be tracks with the same name - artist key should be involved
            const masterEntitiesLookups = await batchLookupMasterEntitiesWithParams(entityType, request);
            rawEntities.forEach(rawEntity => {
                const lookupEntities = masterEntitiesLookups.results[rawEntity.name] ?? [];
                const lookupParams = {
                    search: rawEntity.name,
                    context: RawEntityLookupContextFactory.fromRawEntity(rawEntity)
                };
                queryClient.setQueryData(entityLookupKeys.query('master', entityType, lookupParams), lookupEntities);
            });
        } catch (error) {
            console.log(`Failed to batch update master entities lookup: ${error}`);
        }

        // 4. Load quiz bindings for supported entity types
        if (entityType === 'artist' || entityType === 'track') {
            try {
                const masterIds = rawEntities
                    .map(entity => entity.getMasterEntity()?.id)
                    .filter((id): id is number => id !== undefined);
                
                if (masterIds.length > 0) {
                    const quizBindings = await getQuizBindings(entityType as MusicQuizSupportedEntityType, masterIds);
                    quizBindings.forEach(binding => {
                        queryClient.setQueryData(
                            quizBindingKeys.detail(entityType as MusicQuizSupportedEntityType, binding.masterId),
                            binding
                        );
                    });
                }
            } catch (error) {
                console.log(`Failed to batch update quiz bindings: ${error}`);
            }
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

    /**
     * Prefetches next page in background if not on last page
     */
    const prefetchNextPage = useCallback(async () => {
        if (rawEntitiesPageQuery.data && params.page < rawEntitiesPageQuery.data.page.totalPages - 1) {
            const nextPageParams = { ...params, page: params.page + 1 };
            const nextPageQueryKey = rawEntitiesKeys.list(dataSource, entityType, nextPageParams);
            
            // Only prefetch if not already cached
            const existingData = queryClient.getQueryData(nextPageQueryKey);
            if (!existingData) {
                console.log(`🔄 Prefetching next page: ${params.page + 1}`);
                try {
                    const nextPageData = await loadPageWithRelatedData(nextPageParams);
                    queryClient.setQueryData(nextPageQueryKey, nextPageData);
                } catch (error) {
                    console.log(`Failed to prefetch next page: ${error}`);
                }
            }
        }
    }, [rawEntitiesPageQuery.data, params, dataSource, entityType, queryClient, loadPageWithRelatedData]);

    // Trigger prefetch when current page data is loaded and query is not fetching
    useEffect(() => {
        if (rawEntitiesPageQuery.isSuccess && !rawEntitiesPageQuery.isFetching) {
            // Use setTimeout to avoid blocking the main thread
            const timeoutId = setTimeout(prefetchNextPage, 100);
            return () => clearTimeout(timeoutId);
        }
    }, [rawEntitiesPageQuery.isSuccess, rawEntitiesPageQuery.isFetching, prefetchNextPage]);

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
        if (rawEntitiesPageQuery.data && params.page < rawEntitiesPageQuery.data.page.totalPages - 1) {
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
        if (page >= 0 && rawEntitiesPageQuery.data && page < rawEntitiesPageQuery.data.page.totalPages) {
            setParams(prev => ({ ...prev, page }));
        }
    };

    const updateParams = (updates: Partial<LastfmPageSearchParamsMap[T]>) => {
        setParams(prev => ({
            ...prev,
            ...updates,
            page: 0 // Reset to first page when filters change
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
        search: params.search || '',
        sort: params.sort || 'name,asc',
        isLoading: rawEntitiesPageQuery.isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        updateParams,
        refresh,
    };
}
