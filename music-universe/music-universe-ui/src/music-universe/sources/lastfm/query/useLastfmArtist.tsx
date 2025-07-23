import { useQuery, useQueryClient } from "@tanstack/react-query";
import { masterEntityLookupKeys, rawEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import type {MasterEntityType} from "@/music-universe/music-data/types/master-entities.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {LastfmArtist} from "@/music-universe/sources/lastfm/types";
import {fetchEntity, type SupportedMasterEntityType} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import {lookupMasterEntities} from "@/music-universe/music-data/api/music-data-commons.ts";
import {fetchBoundMasterEntities} from "@/music-universe/music-data/api/music-data-bindings.ts";

export function useLastfmArtist(
    entityType: MasterEntityType,
    rawEntityId: number
) {

    const queryClient = useQueryClient();

    // TODO generify component and make dataSource & entityType props or fields
    const dataSource: DataSource = 'lastfm';

    const rawEntityQueryKey = rawEntitiesKeys.detail(dataSource, entityType, rawEntityId);

    const update = (updatedEntity: LastfmArtist) => {
        queryClient.setQueryData(rawEntityQueryKey, updatedEntity);
    }

    const invalidate = () => {
        queryClient.invalidateQueries({ queryKey: rawEntityQueryKey });
    }

    const rawEntityQuery = useQuery({
        queryKey: rawEntityQueryKey,
        queryFn: async () => {

            // get raw entity
            const rawEntity = await fetchEntity(entityType as SupportedMasterEntityType , rawEntityId) as LastfmArtist;

            // get master entity and lookup for entity name
            const [masterEntityBinding, masterEntityLookup] = await Promise.all([
                fetchBoundMasterEntities(dataSource, entityType, [rawEntityId]), // no single-binding method atm
                lookupMasterEntities(entityType, rawEntity.name),
            ]);

            const masterEntity = masterEntityBinding?.length
                ? masterEntityBinding[0].masterEntity
                : undefined;

            // update entity
            rawEntity.setMasterEntity(masterEntity)

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