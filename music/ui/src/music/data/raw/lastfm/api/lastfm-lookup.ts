import axios from 'axios';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig.ts';
import type { LookupEntity, BaseLookupRequest } from '@/music/shared/types/lookup.ts';

/**
 * Generic LastFM lookup function
 * 
 * @param endpoint API endpoint (e.g., 'artists', 'tags')
 * @param request Lookup request with search string and optional limit
 * @returns Array of lookup results
 */
async function lookupLastfmEntities(endpoint: string, request: BaseLookupRequest): Promise<LookupEntity[]> {
    console.log(`🔍 Looking up LastFM ${endpoint} for: "${request.search}"`);
    
    const response = await axios.get<LookupEntity[]>(
        `${LastfmConfig.baseApiUrl}/${endpoint}/lookup`,
        {
            params: {
                search: request.search,
                limit: request.limit || 20
            }
        }
    );
    
    return response.data;
}

/**
 * Lookup LastFM artists
 */
export async function lookupLastfmArtists(request: BaseLookupRequest): Promise<LookupEntity[]> {
    return lookupLastfmEntities('artists', request);
}

/**
 * Lookup LastFM tags (categories)
 */
export async function lookupLastfmTags(request: BaseLookupRequest): Promise<LookupEntity[]> {
    return lookupLastfmEntities('tags', request);
}
