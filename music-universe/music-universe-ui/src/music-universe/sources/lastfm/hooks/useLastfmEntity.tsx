import { useRawEntity } from "@/music-universe/shared/hooks/useRawEntity";
import { fetchLastfmEntity } from "@/music-universe/sources/lastfm/api/lastfm-common-fetching";
import type { LastfmSupportedEntityType } from "@/music-universe/sources/lastfm/types/lastfm-entity";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";

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
