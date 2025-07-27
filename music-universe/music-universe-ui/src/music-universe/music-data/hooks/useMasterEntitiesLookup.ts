import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useQuery} from "@tanstack/react-query";
import {masterEntityLookupKeys} from "@/music-universe/shared/utils/query-keys.ts";
import {lookupMasterEntities} from "@/music-universe/music-data/api/music-data-lookup.ts";
import type {LookupRequestSourceParams} from "@/music-universe/music-data/types/master-entities-lookup.ts";

export function useMasterEntitiesLookup<T extends MasterEntityType>(
    entityType: MasterEntityType,
    lookupParams: LookupRequestSourceParams<T>
) {

    const lookupQueryKey = masterEntityLookupKeys.query(entityType, lookupParams.search);
    const lookupQuery = useQuery({
            queryKey: lookupQueryKey,
            queryFn: async () => {
                return await lookupMasterEntities(entityType, lookupParams);
            }
        }
    )

    return {
        currentOptions: lookupQuery.data,
        isLoading: lookupQuery.isLoading
    }
}