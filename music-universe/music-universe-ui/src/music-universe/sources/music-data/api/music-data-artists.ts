import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { BoundEntity } from '@/music-universe/shared/types/bindable';

export interface BoundArtist {
    externalId: number;
    dataSource: string;
    referenceId: number;
    referenceName: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data: T;
}

export interface ArtistBindingRequest {
    name: string;
    description?: string;
    imageUrl?: string;
}

/**
 * Fetches bound artists from the music-data API
 * 
 * @param externalIds List of external IDs to check
 * @returns List of bound artists
 */
export async function fetchBoundArtists(externalIds: number[]): Promise<BoundArtist[]> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/artists/bound/LASTFM`;
        const response = await axios.get<ApiResponse<BoundArtist[]>>(
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
        
        const response = await axios.post<ApiResponse<BoundArtist>>(
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
