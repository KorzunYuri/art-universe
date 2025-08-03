import type {SearchParams} from "@/music-universe/shared/components/BaseEntityTable/BaseEntityTable.tsx";
import {useState} from "react";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {masterEntityLookupKeys, rawEntitiesKeys} from "@/music-universe/shared/utils/query-keys.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {fetchBoundMasterEntities} from "@/music-universe/music-data/api/music-data-binding.ts";
import type {LastfmSupportedEntityType} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";
import {batchLookupMasterEntities} from "@/music-universe/music-data/api/music-data-lookup.ts";
import {
    fetchLastfmEntities,
    type LastfmPageSearchParamsMap
} from "@/music-universe/sources/lastfm/api/lastfm-common-fetching.ts";
import {LookupRequestSourceParams} from "@/music-universe/music-data/types/master-entities-lookup.ts";

export function useLastfmEntityTable<T extends LastfmSupportedEntityType>(
    entityType: T,
    initialParams: Partial<LastfmPageSearchParamsMap[T]> = {}
) {
    const queryClient = useQueryClient();
    const [params, setParams] = useState<SearchParams>({
        page: 0,
        size: 20,
        search: '',
        sort: 'name,asc',
        ...initialParams,
    });

    // Constants for this component
    const dataSource: DataSource = 'lastfm';

    const rawEntitiesPageQueryKey = rawEntitiesKeys.list(dataSource, entityType, params);
    const rawEntitiesPageQuery = useQuery({
        queryKey: rawEntitiesPageQueryKey,
        queryFn: async () => {

            console.log("batch artist fetch triggered")

            // get raw entities
            const rawEntitiesPage = await fetchLastfmEntities<T>(entityType, params);
            const rawEntities = rawEntitiesPage.content;

            // attach master entities to raw entities
            const combinedEntities = [...rawEntities];
            const rawEntityIds = rawEntities.map((entity) => entity.id);
            fetchBoundMasterEntities(dataSource, entityType, rawEntityIds)
                .then(masterEntityBindings => {
                    // merge the changes
                    combinedEntities.forEach((raw) => {
                        const boundEntityInfo = masterEntityBindings.find((e) => e.externalId === raw.id);
                        // @ts-expect-error TS2345: Argument of type A | B is not assignable to type A & B
                        raw.setMasterEntity(boundEntityInfo?.masterEntity);
                    });

                    // update the cache with single raw entities, for single row usage
                    combinedEntities.forEach((e) => {
                        queryClient.setQueryData(rawEntitiesKeys.detail(dataSource, entityType, e.id), e);
                    })
                })

            try {
                // collecting names is needed to init cache with empty lists later, as backend doesn't return them
                const rawEntityNames = rawEntities.map((entity) => entity.name);
                const request = rawEntities.map(
                    entity => new LookupRequestSourceParams(entity.name, entity))
                batchLookupMasterEntities(entityType, request)
                    .then(masterEntitiesLookups => {
                        // update the cache for master entities lookup
                        rawEntityNames.forEach(name => {
                            const lookupEntities = masterEntitiesLookups.results[name] ?? [];
                            queryClient.setQueryData(masterEntityLookupKeys.query(entityType, name), lookupEntities);
                        })
                    });
            } catch {
                console.log("Failed to get lookups")
            }

            return {
                page: { ...rawEntitiesPage, content: combinedEntities },
                rawEntityIds: rawEntityIds,
            };
        }
    })

    // Functions for managing parameters
    const setSearch = (search: string) => setParams(prev => ({...prev, search, page: 0}));
    const setSort = (sort: string) => setParams(prev => ({...prev, sort}));
    const nextPage = () => {
        if (rawEntitiesPageQuery.data && params.page < rawEntitiesPageQuery.data.page.totalPages - 1) {
            setParams(prev => ({...prev, page: prev.page + 1}));
        }
    };
    const prevPage = () => {
        if (params.page > 0) {
            setParams(prev => ({...prev, page: prev.page - 1}));
        }
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
        search: params.search,
        sort: params.sort,
        isLoading: rawEntitiesPageQuery.isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        refresh,
    };
}