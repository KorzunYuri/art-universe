import type {SearchParams} from "@/music-universe/shared/components/BaseEntityTable/BaseEntityTable.tsx";
import {useState} from "react";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {fetchArtists} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import {masterEntityLookupKeys, rawEntitiesKeys} from "@/music-universe/shared/utils/query-keys.ts";
import {batchLookupArtists, fetchBoundArtists} from "@/music-universe/music-data/api/music-data-artists.ts";
import {createMasterEntityFromBinding} from "@/music-universe/music-data/utils/master-entities-common.ts";

export function useLastfmArtistsTable(initialParams: Partial<SearchParams> = {}) {
    const queryClient = useQueryClient();
    const [params, setParams] = useState<SearchParams>({
        page: 0,
        size: 20,
        search: '',
        sort: 'name,asc',
        ...initialParams,
    });

    // Constants for this component
    const dataSource = 'LASTFM';
    const entityType = "artist";

    const rawEntitiesPageQueryKey = rawEntitiesKeys.list(dataSource, entityType, params);
    const rawEntitiesPageQuery = useQuery({
        queryKey: rawEntitiesPageQueryKey,
        queryFn: async () => {

            console.log("batch artist fetch triggered")

            // get raw entities
            const rawEntitiesPage = await fetchArtists(params);
            const rawEntities = rawEntitiesPage.content;
            const rawEntityIds = rawEntities.map((entity) => entity.id);
            const rawEntityNames = rawEntities.map((entity) => entity.name);

            // get master entities
            const [masterEntityBindings, masterEntitiesLookups] = await Promise.all([
                fetchBoundArtists(rawEntityIds),
                batchLookupArtists(rawEntityNames),
            ]);

            const rawIdToMasterEntityMap = new Map(
                masterEntityBindings.map((e) => [e.externalId, createMasterEntityFromBinding(e, entityType)])
            );

            // merge the changes
            const combinedEntities = rawEntities.map((raw) => {
                raw.setMasterEntity(rawIdToMasterEntityMap.get(raw.id));
                return raw;
            });

            // update the cache with single raw entities, for single row usage
            combinedEntities.forEach((e) => {
                queryClient.setQueryData(rawEntitiesKeys.detail(dataSource, entityType, e.id), e);
            })

            // update the cache for master entities lookup
            for (const [name, entities] of Object.entries(masterEntitiesLookups.results)) {
                queryClient.setQueryData(masterEntityLookupKeys.query(entityType, name), entities);
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