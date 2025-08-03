import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {masterEntityLookupKeys} from "@/music-universe/shared/utils/query-keys.ts";
import {useState} from "react";
import {lookupMasterEntities} from "@/music-universe/music-data/api/music-data-lookup.ts";
import type {LookupRequestSourceParams} from "@/music-universe/music-data/types/master-entities-lookup.ts";

export function useMasterEntitiesLookup<T extends MasterEntityType>(
    entityType: MasterEntityType,
    initialLookupParams: LookupRequestSourceParams<T>
) {

    const [lookupParams, setLookupParams] = useState(initialLookupParams);
    const queryClient = useQueryClient();

    const lookupQueryKey = masterEntityLookupKeys.query(entityType, lookupParams.search);
    const lookupQuery = useQuery({
            queryKey: lookupQueryKey,
            queryFn: async () => {
                 const lookupEntities = await lookupMasterEntities(entityType, lookupParams);
                 queryClient.setQueryData(lookupQueryKey, lookupEntities);

                 return lookupEntities;
            }
        }
    )

    return {
        currentOptions: lookupQuery.data,
        setRequest: setLookupParams,
        isLoading: lookupQuery.isLoading
    }
}