import { useQuery, useQueryClient } from "@tanstack/react-query";
import { masterEntityLookupKeys, rawEntitiesKeys } from "@/music-universe/shared/utils/query-keys.ts";
import { fetchArtist } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import {
    fetchBoundArtists,
    lookupArtists
} from "@/music-universe/music-data/api/music-data-artists.ts";
import { createMasterEntityFromBinding } from "@/music-universe/music-data/utils/master-entities-common.ts";
import type { MasterEntityType } from "@/music-universe/music-data/types/master-entities.ts";

export function useLastfmArtist(rawEntityId: number) {

    const queryClient = useQueryClient();

    // Constants for this component
    const dataSource = 'LASTFM';
    const entityType: MasterEntityType = 'artist';

    const rawEntityQueryKey = rawEntitiesKeys.detail(dataSource, entityType, rawEntityId);
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
        entity:         rawEntityQuery?.data,
        isLoading:      rawEntityQuery.isLoading,
        isError:        rawEntityQuery.isError,
        errorMessage:   rawEntityQuery.error,
    };
}