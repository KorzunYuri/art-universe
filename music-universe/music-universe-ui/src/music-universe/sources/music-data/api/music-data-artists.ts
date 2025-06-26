import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { BoundEntity, BoundEntityResponse } from '@/music-universe/shared/types/bindable';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { SearchableEntity } from '@/music-universe/shared/components/AutocompleteInput';

export interface ArtistBindingRequest {
    name: string;
    description?: string;
    imageUrl?: string;
}

/**
 * Searches for artists in Music Data by name
 * 
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching artists
 */
export async function searchArtists(query: string, limit: number = 10): Promise<ApiResponse<SearchableEntity[]>> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/artists/search`;
        const params = { query: query, limit: limit };

        const response = await axios.get<ApiResponse<SearchableEntity[]>>(url, { params });
        
        return response.data;
    } catch (error) {
        console.error('❌ Error searching artists:', error);
        if (axios.isAxiosError(error)) {
            console.error('❌ Axios error details:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
        }
        return {
            success: false,
            message: 'Failed to search artists',
            data: []
        };
    }
}

/**
 * Fetches bound artists from the music-data API
 * 
 * @param externalIds List of external IDs to check
 * @returns List of bound artists
 */
export async function fetchBoundArtists(externalIds: number[]): Promise<BoundEntityResponse[]> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/artists/bound/LASTFM`;
        const response = await axios.get<ApiResponse<BoundEntityResponse[]>>(
            url,
            {
                params: {
                    externalIds: externalIds.join(','),
                },
            }
        );

        if (response.data.success) {
            console.log(`🎯 Found ${response.data.data.length} bound artists`);
            return response.data.data;
        } else {
            console.warn(`⚠️ API returned success=false: ${response.data.message}`);
            return [];
        }
    } catch (error) {
        console.error('❌ Error fetching bound artists:', error);
        return [];
    }
}

/**
 * Binds an artist from LastFM to an artist in music-data
 * 
 * @param externalId The LastFM artist ID
 * @param artistName The name of the artist
 * @returns The bound artist if successful, null otherwise
 */
export async function bindArtist(externalId: number, artistName: string): Promise<BoundEntity | null> {
    try {
        console.log(`🔗 Binding artist ${externalId} with name "${artistName}"`);
        const request: ArtistBindingRequest = { name: artistName };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/artists/bind/LASTFM/${externalId}`,
            request
        );
        
        if (response.data.success && response.data.data) {
            return {
                referenceId: response.data.data.referenceId,
                referenceName: response.data.data.referenceName
            };
        }
        
        return null;
    } catch (error) {
        console.error('❌ Error binding artist:', error);
        return null;
    }
}

/**
 * Unbinds an artist from LastFM
 * 
 * @param externalId The LastFM artist ID
 * @returns True if successful, false otherwise
 */
export async function unbindArtist(externalId: number): Promise<boolean> {
    try {
        console.log(`🔓 Unbinding artist ${externalId}`);
        
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/artists/unbind/LASTFM/${externalId}`
        );
        
        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error unbinding artist:', error);
        return false;
    }
}
