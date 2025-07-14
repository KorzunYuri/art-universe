import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { BoundEntity, BoundEntityResponse } from '@/music-universe/shared/types/bindable';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { LookupEntity, BatchLookupRequestDTO, BatchLookupResponseDTO } from '@/music-universe/shared/types/lookup';

export interface ArtistBindToExistingRequest {
    artistId: number;
}

export interface ArtistCreateAndBindRequest {
    name: string;
}

/**
 * Searches for artists in Music Data by name
 * 
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching artists
 */
export async function lookupArtists(query: string, limit: number = 10): Promise<ApiResponse<LookupEntity[]>> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/artists/lookup`;
        const params = { name: query, limit: limit };

        const response = await axios.get<ApiResponse<LookupEntity[]>>(url, { params });
        
        return response.data;
    } catch (error) {
        console.error('❌ Error looking up artists:', error);
        if (axios.isAxiosError(error)) {
            console.error('❌ Axios error details:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
        }
        return {
            success: false,
            message: 'Failed to look up artists',
            data: []
        };
    }
}

/**
 * Performs batch lookup of artists by multiple names
 * 
 * @param names Array of artist names to look up
 * @param limit Maximum number of results for each name (default: 10)
 * @returns Object with lookup results grouped by artist names
 */
export async function batchLookupArtists(names: string[], limit: number = 10): Promise<ApiResponse<BatchLookupResponseDTO>> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/artists/lookup/batch`;
        const request: BatchLookupRequestDTO = { 
            names, 
            limit 
        };

        console.log(`🔍 Batch looking up ${names.length} artists`);
        const response = await axios.post<ApiResponse<BatchLookupResponseDTO>>(url, request);
        
        if (response.data.success) {
            const resultCount = Object.keys(response.data.data.results).length;
            console.log(`✅ Batch lookup successful: found matches for ${resultCount} artists`);
        }
        
        return response.data;
    } catch (error) {
        console.error('❌ Error batch looking up artists:', error);
        if (axios.isAxiosError(error)) {
            console.error('❌ Axios error details:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
        }
        return {
            success: false,
            message: 'Failed to batch lookup artists',
            data: { results: {} }
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
 * Binds an artist from LastFM to an existing artist in music-data
 * 
 * @param externalId The LastFM artist ID
 * @param artistId The existing artist ID in music-data
 * @returns The bound artist if successful, null otherwise
 */
export async function bindArtistToExisting(externalId: number, artistId: number): Promise<BoundEntity | null> {
    try {
        console.log(`🔗 Binding artist ${externalId} to existing artist ${artistId}`);
        const request: ArtistBindToExistingRequest = { artistId };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/artists/bind/existing/LASTFM/${externalId}`,
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
        console.error('❌ Error binding artist to existing:', error);
        return null;
    }
}

/**
 * Creates a new artist and binds it to LastFM artist
 * 
 * @param externalId The LastFM artist ID
 * @param artistName The name of the new artist
 * @returns The bound artist if successful, null otherwise
 */
export async function createAndBindArtist(externalId: number, artistName: string): Promise<BoundEntity | null> {
    try {
        console.log(`🔗 Creating and binding new artist ${externalId} with name "${artistName}"`);
        const request: ArtistCreateAndBindRequest = { name: artistName };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/artists/bind/new/LASTFM/${externalId}`,
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
        console.error('❌ Error creating and binding artist:', error);
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
