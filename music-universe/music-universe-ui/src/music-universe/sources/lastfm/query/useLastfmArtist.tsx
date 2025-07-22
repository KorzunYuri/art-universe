import { useQuery, useQueryClient } from "@tanstack/react-query";
import { masterEntityLookupKeys, rawEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import { fetchArtist } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import {
    fetchBoundArtists,
    lookupArtists
} from "@/music-universe/music-data/api/music-data-artists.ts";
import { createMasterEntityFromBinding } from "@/music-universe/music-data/utils/master-entities-common.ts";
import type { MasterEntityType } from "@/music-universe/music-data/types/master-entities.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {LastfmArtist} from "@/music-universe/sources/lastfm/types";

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
            const rawEntity = await fetchArtist(rawEntityId);

            // get master entity and lookup for entity name
            const [masterEntityBinding, masterEntityLookup] = await Promise.all([
                fetchBoundArtists([rawEntityId]), // no single-binding method atm
                lookupArtists(rawEntity.name),
            ]);

            const masterEntity = masterEntityBinding?.length
                ? createMasterEntityFromBinding(masterEntityBinding[0], entityType)
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