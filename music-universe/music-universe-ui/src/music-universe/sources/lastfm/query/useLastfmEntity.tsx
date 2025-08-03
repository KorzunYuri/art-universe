import { useQuery, useQueryClient } from "@tanstack/react-query";
import { masterEntityLookupKeys, rawEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {
    fetchLastfmEntity,
} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import {lookupMasterEntities} from "@/music-universe/music-data/api/music-data-commons.ts";
import {fetchBoundMasterEntities} from "@/music-universe/music-data/api/music-data-binding.ts";
import type {
    LastfmSupportedEntityType,
    LastfmSupportedEntityTypeMap
} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";

export function useLastfmEntity<T extends LastfmSupportedEntityType>(
    entityType: T,
    rawEntityId: number
) {
    const dataSource: DataSource = "lastfm";
    const queryClient = useQueryClient();
    const rawEntityQueryKey = rawEntitiesKeys.detail(dataSource, entityType, rawEntityId);

    const update = (updatedEntity: LastfmSupportedEntityTypeMap[T]) => {
        queryClient.setQueryData(rawEntityQueryKey, updatedEntity);
    }

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: rawEntityQueryKey });
    }

    const rawEntityQuery = useQuery({
        queryKey: rawEntityQueryKey,
        queryFn: async () => {
            // get raw entity
            const rawEntity = await fetchLastfmEntity<T>(entityType, rawEntityId);

            // get master entity and lookup for entity name
            const [masterEntityBinding, masterEntityLookup] = await Promise.all([
                fetchBoundMasterEntities<T>(dataSource, entityType, [rawEntityId]),
                lookupMasterEntities(entityType, rawEntity.name),
            ]);

            const masterEntity = masterEntityBinding?.length
                ? masterEntityBinding[0].masterEntity
                : undefined;

            // @ts-expect-error LastfmSupportedMasterEntityType is a subset of MasterEntityType and raw/master entity types correspond to each other
            rawEntity.setMasterEntity(masterEntity);

            // update the cache for master entities lookup
            const masterEntityLookupQueryKey = masterEntityLookupKeys.query(entityType, rawEntity.name);
            queryClient.setQueryData(masterEntityLookupQueryKey, masterEntityLookup);

            return rawEntity;
        }
    })

    return {
        entity:             rawEntityQuery?.data,
        updateEntity:       update,
        invalidateEntity:   invalidate,
        isLoading:          rawEntityQuery.isLoading,
        isError:            rawEntityQuery.isError,
        error:              rawEntityQuery.error,
    };
}
