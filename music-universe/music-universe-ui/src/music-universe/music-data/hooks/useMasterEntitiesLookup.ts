import { useEntityLookup } from "@/music-universe/shared/hooks/useEntityLookup";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import type {BasicMasterEntityLookupParams} from "@/music-universe/music-data/types/music-data-lookup-types.ts";


/**
 * Hook for looking up master entities
 * This is a convenience wrapper around useEntityLookup for master entities
 */
export function useMasterEntitiesLookup(
    entityType: MasterEntityType,
    params: BasicMasterEntityLookupParams,
    enableEmptySearch = false
) {
    return useEntityLookup('master', entityType, params, enableEmptySearch);
}
