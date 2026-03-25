import { LookupRegistry } from "@/shared/services/LookupRegistry.ts";
import {
    lookupLastfmArtists,
    lookupLastfmTags
} from "@/music/data/raw/lastfm/api/lastfm-lookup.ts";
import type {
    LastfmBasicLookupParams,
    LastfmBasicLookupRequest
} from "@/music/data/raw/lastfm/types/lastfm-lookup-types.ts";
import { registerRawEntityFetcher } from "@/music/data/raw/shared/registry/rawEntityFetchRegistry.ts";
import { fetchLastfmEntity } from "@/music/data/raw/lastfm/api/lastfm-common-fetching.ts";

/**
 * Registers lookup configurations for LastFM entities
 * This function is called during app initialization to register all LastFM entity lookups
 */
export function registerLastfmLookups() {
    console.log('🔧 Registering LastFM entity lookups...');

    registerRawEntityFetcher('lastfm', (entityType, id) => fetchLastfmEntity(entityType as any, id));

    LookupRegistry.register('lastfm', 'artist', {
        transformParams: (params: LastfmBasicLookupParams): LastfmBasicLookupRequest => ({
            search: params.search,
            limit: params.limit
        }),
        
        lookupEntities: lookupLastfmArtists
    });

    LookupRegistry.register('lastfm', 'category', {
        transformParams: (params: LastfmBasicLookupParams): LastfmBasicLookupRequest => ({
            search: params.search,
            limit: params.limit
        }),
        
        lookupEntities: lookupLastfmTags
    });

    console.log('✅ LastFM entity lookups registered');
}
