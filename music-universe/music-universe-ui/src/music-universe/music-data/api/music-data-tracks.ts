import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { MasterEntity } from '@/music-universe/shared/types/entities.ts';
import {
    type BoundEntityResponse,
    type TrackBoundEntityResponse,
    createMasterEntityFromBinding,
} from '@/music-universe/music-data/utils/master-entities-common.ts';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import type {MasterEntityType} from "@/music-universe/music-data/types/master-entities.ts";

const entityType: MasterEntityType = 'track'

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
export async function lookupTracks(query: string, limit: number = 10): Promise<LookupEntity[]> {
    const response = await axios.get<LookupEntity[]>(
        `${MusicDataConfig.baseApiUrl}/tracks`,
        {
            params: {
                search: query,
                limit: limit
            }
        }
    );

    return response.data;
}

/**
 * Fetches bound tracks from the music-data API
 * 
 * @param externalIds List of external IDs to check
 * @returns List of bound tracks
 */
export async function fetchBoundTracks(externalIds: number[]): Promise<BoundEntityResponse[]> {
    const url = `${MusicDataConfig.baseApiUrl}/tracks/bound/LASTFM`;
    const response = await axios.get<BoundEntityResponse[]>(
        url,
        {
            params: {
                externalIds: externalIds.join(','),
            },
        }
    );

    return response.data;
}

/**
 * Binds a track from LastFM to a track in music-data
 *
 * @param externalId The LastFM track ID
 * @param trackName The name of the track
 * @param artistExternalId The LastFM artist ID
 * @returns The bound track if successful, null otherwise
 */
export async function bindTrack(externalId: number, trackName: string, artistExternalId: number): Promise<MasterEntity | null> {
    const request: TrackBindingRequest = {
        name: trackName,
        artistExternalId: artistExternalId
    };

    const response = await axios.post<TrackBoundEntityResponse>(
        `${MusicDataConfig.baseApiUrl}/tracks/bind/LASTFM/${externalId}`,
        request
    );

    return createMasterEntityFromBinding(response.data, entityType);
}

/**
 * Unbinds a track from LastFM
 *
 * @param externalId The LastFM track ID
 * @returns True if successful, false otherwise
 */
export async function unbindTrack(externalId: number): Promise<boolean> {
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/tracks/unbind/LASTFM/${externalId}`
    );

    return response.data;
}
