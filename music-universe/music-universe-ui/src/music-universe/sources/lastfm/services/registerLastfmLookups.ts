import { LookupRegistry } from "@/music-universe/shared/services/LookupRegistry";
import { 
    lookupLastfmArtists, 
    lookupLastfmTags
} from "@/music-universe/sources/lastfm/api/lastfm-lookup";
import type {
    LastfmBasicLookupParams,
    LastfmBasicLookupRequest
} from "@/music-universe/sources/lastfm/types/lastfm-lookup-types";

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
