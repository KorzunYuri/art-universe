import { useQuery, useQueryClient } from "@tanstack/react-query";
import { masterEntityLookupKeys, rawEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {fetchBoundMasterEntities} from "@/music-universe/music-data/api/music-data-binding.ts";
import type {
    LastfmSupportedEntityType,
    LastfmSupportedEntityTypeMap
} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";
import {
    lookupMasterEntities,
} from "@/music-universe/music-data/api/music-data-lookup.ts";
import {fetchLastfmEntity} from "@/music-universe/sources/lastfm/api/lastfm-common-fetching.ts";
import type {LookupRequestSourceParams} from "@/music-universe/music-data/types/master-entities-lookup.ts";

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

            fetchBoundMasterEntities<T>(dataSource, entityType, [rawEntityId])
                .then(masterEntityBinding => {
                    // assign master entity
                    const masterEntity = masterEntityBinding?.length
                        ? masterEntityBinding[0].masterEntity
                        : undefined;

                    // @ts-expect-error TS2345: Argument of type A | B is not assignable to type A & B
                    rawEntity.setMasterEntity(masterEntity);

                });

            try {
                // @ts-expect-error TS2345: LastfmSupportedEntityType cannot be cast to MasterEntityType
                const searchSourceParams = new LookupRequestSourceParams<T>(rawEntity.name, rawEntity);
                lookupMasterEntities(entityType, searchSourceParams)
                    .then(masterEntityLookup => {
                            // update the cache for master entities lookup
                            const masterEntityLookupQueryKey = masterEntityLookupKeys.query(entityType, rawEntity.name);
                            queryClient.setQueryData(masterEntityLookupQueryKey, masterEntityLookup);
                    });
            } catch {
                console.log("Failed to update master entities lookup")
            }

            return rawEntity;
        }
    })

    return {
        entity:             rawEntityQuery.data,
        updateEntity:       update,
        invalidateEntity:   invalidate,
        isLoading:          rawEntityQuery.isLoading,
        isError:            rawEntityQuery.isError,
        error:              rawEntityQuery.error,
    };
}
