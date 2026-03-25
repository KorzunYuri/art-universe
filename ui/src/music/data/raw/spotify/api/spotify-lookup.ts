import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig.ts';
import type { LookupEntity, BaseLookupRequest } from '@/shared/types/lookup.ts';

const spotifyReadApi = SpotifyConfig.readApi;

export async function lookupSpotifyArtists(request: BaseLookupRequest): Promise<LookupEntity[]> {
    const response = await spotifyReadApi.get<LookupEntity[]>(
        '/artists/lookup',
        {
            params: {
                search: request.search,
                limit: request.limit || 20
            }
        }
    );

    return response.data;
}
