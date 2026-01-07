import { LookupRegistry } from "@/music/shared/services/LookupRegistry.ts";
import { 
    lookupLastfmArtists, 
    lookupLastfmTags
} from "@/music/data/raw/lastfm/api/lastfm-lookup.ts";
import type {
    LastfmBasicLookupParams,
    LastfmBasicLookupRequest
} from "@/music/data/raw/lastfm/types/lastfm-lookup-types.ts";

/**
 * Registers lookup configurations for LastFM entities
 * This function is called during app initialization to register all LastFM entity lookups
 */
export function registerLastfmLookups() {
    console.log('🔧 Registering LastFM entity lookups...');

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
