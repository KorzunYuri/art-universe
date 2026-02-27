import { useRawEntity } from "@/music/shared/hooks/useRawEntity.tsx";
import { fetchLastfmEntity } from "@/music/data/raw/lastfm/api/lastfm-common-fetching.ts";
import type { LastfmSupportedEntityType } from "@/music/data/raw/lastfm/types/lastfm-entity.ts";
import type {MasterEntityType} from "@/music/shared/types/entities.ts";

/**
 * Hook for fetching and managing LastFM entities
 * This is a specialized wrapper around useRawEntity for LastFM data source
 * 
 * @param entityType The type of entity to fetch (e.g., 'artist', 'track', 'category')
 * @param entityId The ID of the entity to fetch
 * @returns Object with entity data and utility functions
 */
export function useLastfmEntity<T extends LastfmSupportedEntityType>(
    entityType: T,
    entityId: number
) {
    // Create a fetch function for the LastFM entity
    const fetchFn = () => fetchLastfmEntity(entityType, entityId);
    
    // Use the generic useRawEntity hook with LastFM-specific parameters
    return useRawEntity('lastfm', entityType as MasterEntityType, entityId, fetchFn);
}
