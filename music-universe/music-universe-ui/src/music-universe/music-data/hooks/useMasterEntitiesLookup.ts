import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {masterEntityLookupKeys} from "@/music-universe/shared/utils/query-keys.ts";
import {lookupMasterEntities} from "@/music-universe/music-data/api/music-data-commons.ts";
import {useState} from "react";

export function useMasterEntitiesLookup(entityType: MasterEntityType, searchString: string) {

    const [query, setQuery] = useState(searchString);

    const queryClient = useQueryClient();

    const lookupQueryKey = masterEntityLookupKeys.query(entityType, query);
    const lookupQuery = useQuery({
            queryKey: lookupQueryKey,
            queryFn: async () => {
                 const lookupEntities = await lookupMasterEntities(entityType, query);
                 queryClient.setQueryData(lookupQueryKey, lookupEntities);

                 return lookupEntities;
            }
        }
    )

    return {
        currentOptions: lookupQuery.data,
        setQuery,
        isLoading: lookupQuery.isLoading
    }
}