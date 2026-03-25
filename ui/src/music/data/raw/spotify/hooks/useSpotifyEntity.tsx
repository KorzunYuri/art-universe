import { useRawEntity } from "@/music/shared/hooks/useRawEntity.tsx";
import { fetchSpotifyEntity } from "@/music/data/raw/spotify/api/spotify-common-fetching.ts";
import type { SpotifySupportedEntityType } from "@/music/data/raw/spotify/types/spotify-entity.ts";
import type { MusicMasterEntityType } from "@/music/data/master/api/music-data-commons.ts";

/**
 * Hook for fetching and managing Spotify entities.
 * Specialized wrapper around useRawEntity for the Spotify data source.
 */
export function useSpotifyEntity<T extends SpotifySupportedEntityType>(
    entityType: T,
    entityId: number
) {
    const fetchFn = () => fetchSpotifyEntity(entityType, entityId);
    return useRawEntity('spotify', entityType as MusicMasterEntityType, entityId, fetchFn);
}
