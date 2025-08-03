import type {SearchParams} from "@/music-universe/shared/components/BaseEntityTable/BaseEntityTable.tsx";
import {useState} from "react";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {masterEntityLookupKeys, rawEntitiesKeys} from "@/music-universe/shared/utils/query-keys.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import { batchLookupMasterEntities } from "@/music-universe/music-data/api/music-data-commons.ts";
import {fetchBoundMasterEntities} from "@/music-universe/music-data/api/music-data-binding.ts";
import {fetchLastfmEntities} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import type {LastfmSupportedEntityType} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";

export function useLastfmEntityTable<T extends LastfmSupportedEntityType>(
    entityType: T,
    initialParams: Partial<SearchParams> = {}
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
            const rawEntityIds = rawEntities.map((entity) => entity.id);
            const rawEntityNames = rawEntities.map((entity) => entity.name);

            // get master entities
            const [masterEntityBindings, masterEntitiesLookups] = await Promise.all([
                fetchBoundMasterEntities(dataSource, entityType, rawEntityIds),
                batchLookupMasterEntities(entityType, rawEntityNames),
            ]);

            // merge the changes
            const combinedEntities = rawEntities.map((raw) => {
                const boundEntityInfo = masterEntityBindings.find((e) => e.externalId === raw.id);
                // @ts-expect-error LastfmSupportedMasterEntityType is a subset of MasterEntityType and raw/master entity types correspond to each other
                raw.setMasterEntity(boundEntityInfo?.masterEntity);
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