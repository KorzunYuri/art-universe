import { LookupRegistry } from "@/shared/services/LookupRegistry.ts";
import { lookupSpotifyArtists } from "@/music/data/raw/spotify/api/spotify-lookup.ts";
import type { BaseLookupRequest } from "@/shared/types/lookup.ts";
import { registerRawEntityFetcher } from "@/music/data/raw/shared/registry/rawEntityFetchRegistry.ts";
import { fetchSpotifyEntity } from "@/music/data/raw/spotify/api/spotify-common-fetching.ts";

export function registerSpotifyLookups() {
    registerRawEntityFetcher('spotify', (entityType, id) => fetchSpotifyEntity(entityType as any, id));

    LookupRegistry.register('spotify', 'artist', {
        transformParams: (params: { search: string; limit?: number }): BaseLookupRequest => ({
            search: params.search,
            limit: params.limit
        }),

        lookupEntities: lookupSpotifyArtists
    });
}
