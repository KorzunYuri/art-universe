import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { BoundEntity, BoundEntityResponse } from '@/music-universe/shared/types/bindable';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { LookupEntity } from "@/music-universe/shared/components";

export interface TrackBindingRequest {
    name: string;
    artistExternalId: number;
}

/**
 * Searches for tracks in Music Data by name
 * Note: This is a placeholder implementation - the actual API endpoint may not exist yet
 * 
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching tracks
 */
export async function lookupTracks(query: string, limit: number = 10): Promise<ApiResponse<LookupEntity[]>> {
    try {
        const response = await axios.get<ApiResponse<LookupEntity[]>>(
            `${MusicDataConfig.baseApiUrl}/tracks`,
            {
                params: {
                    search: query,
                    limit: limit
                }
            }
        );
        
        return response.data;
    } catch (error) {
        console.error('❌ Error searching tracks:', error);
        return {
            success: false,
            message: 'Failed to search tracks',
            data: []
        };
    }
}

/**
 * Fetches bound tracks from the music-data API
 * 
 * @param externalIds List of external IDs to check
 * @returns List of bound tracks
 */
export async function fetchBoundTracks(externalIds: number[]): Promise<BoundEntityResponse[]> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/tracks/bound/LASTFM`;
        const response = await axios.get<ApiResponse<BoundEntityResponse[]>>(
            url,
            {
                params: {
                    externalIds: externalIds.join(','),
                },
            }
        );

        if (response.data.success) {
            console.log(`🎯 Found ${response.data.data.length} bound tracks`);
            return response.data.data;
        } else {
            console.warn(`⚠️ API returned success=false: ${response.data.message}`);
            return [];
        }
    } catch (error) {
        console.error('❌ Error fetching bound tracks:', error);
        return [];
    }
}

/**
 * Binds a track from LastFM to a track in music-data
 *
 * @param externalId The LastFM track ID
 * @param trackName The name of the track
 * @param artistExternalId The LastFM artist ID
 * @returns The bound track if successful, null otherwise
 */
export async function bindTrack(externalId: number, trackName: string, artistExternalId: number): Promise<BoundEntity | null> {
    try {
        console.log(`🔗 Binding track ${externalId} with name "${trackName}" and artist ${artistExternalId}`);
        const request: TrackBindingRequest = {
            name: trackName,
            artistExternalId: artistExternalId
        };

        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/tracks/bind/LASTFM/${externalId}`,
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
        console.error('❌ Error binding track:', error);
        return null;
    }
}

/**
 * Unbinds a track from LastFM
 *
 * @param externalId The LastFM track ID
 * @returns True if successful, false otherwise
 */
export async function unbindTrack(externalId: number): Promise<boolean> {
    try {
        console.log(`🔓 Unbinding track ${externalId}`);

        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/tracks/unbind/LASTFM/${externalId}`
        );

        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error unbinding track:', error);
        return false;
    }
}
