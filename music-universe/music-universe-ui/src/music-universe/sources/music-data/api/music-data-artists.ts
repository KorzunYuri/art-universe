import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';

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
        const response = await axios.get<ApiResponse<BoundArtist[]>>(
            `${MusicDataConfig.baseApiUrl}/artists/bound/LASTFM`,
            {
                params: {
                    externalIds: externalIds.join(','),
                },
            }
        );
        
        return response.data.success ? response.data.data : [];
    } catch (error) {
        console.error('Error fetching bound artists:', error);
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
export async function bindArtist(externalId: number, artistName: string): Promise<BoundArtist | null> {
    try {
        const request: ArtistBindingRequest = { name: artistName };
        
        const response = await axios.post<ApiResponse<BoundArtist>>(
            `${MusicDataConfig.baseApiUrl}/artists/bind/LASTFM/${externalId}`,
            request
        );
        
        return response.data.success ? response.data.data : null;
    } catch (error) {
        console.error('Error binding artist:', error);
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
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/artists/unbind/LASTFM/${externalId}`
        );
        
        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('Error unbinding artist:', error);
        return false;
    }
}
