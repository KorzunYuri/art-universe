import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { MasterEntity } from '@/music-universe/shared/types/entities.ts';
import {type BoundEntityResponse, createMasterEntityFromBinding} from '@/music-universe/music-data/utils/master-entities-common.ts';
import type { LookupEntity, BatchLookupRequestDTO, BatchLookupResponseDTO } from '@/music-universe/shared/types/lookup';
import type {MasterEntityType} from "@/music-universe/music-data/types/master-entities.ts";

const entityType: MasterEntityType = 'artist'

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
export async function lookupArtists(query: string, limit: number = 10): Promise<LookupEntity[]> {
    const url = `${MusicDataConfig.baseApiUrl}/artists/lookup`;
    const params = { name: query, limit: limit };

    const response = await axios.get<LookupEntity[]>(url, { params });
    return response.data;
}

/**
 * Performs batch lookup of artists by multiple names
 * 
 * @param names Array of artist names to look up
 * @param limit Maximum number of results for each name (default: 10)
 * @returns Object with lookup results grouped by artist names
 */
export async function batchLookupArtists(names: string[], limit: number = 10): Promise<BatchLookupResponseDTO> {
    const url = `${MusicDataConfig.baseApiUrl}/artists/lookup/batch`;
    const request: BatchLookupRequestDTO = { 
        names, 
        limit 
    };

    const response = await axios.post<BatchLookupResponseDTO>(url, request);
    return response.data;
}

/**
 * Fetches bound artists from the music-data API
 * 
 * @param externalIds List of external IDs to check
 * @returns List of bound artists
 */
export async function fetchBoundArtists(externalIds: number[]): Promise<BoundEntityResponse[]> {
    const url = `${MusicDataConfig.baseApiUrl}/artists/bound/LASTFM`;
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
 * Binds an artist from LastFM to an existing artist in music-data
 * 
 * @param externalId The LastFM artist ID
 * @param artistId The existing artist ID in music-data
 * @returns The bound artist if successful, null otherwise
 */
export async function bindArtistToExisting(externalId: number, artistId: number): Promise<MasterEntity> {
    const request: ArtistBindToExistingRequest = { artistId };
    
    const response = await axios.post<BoundEntityResponse>(
        `${MusicDataConfig.baseApiUrl}/artists/bind/existing/LASTFM/${externalId}`,
        request
    );
    
    return createMasterEntityFromBinding(response.data, entityType);
}

/**
 * Creates a new artist and binds it to LastFM artist
 * 
 * @param externalId The LastFM artist ID
 * @param artistName The name of the new artist
 * @returns The bound artist if successful, null otherwise
 */
export async function createAndBindArtist(externalId: number, artistName: string): Promise<MasterEntity> {
    const request: ArtistCreateAndBindRequest = { name: artistName };
    
    const response = await axios.post<BoundEntityResponse>(
        `${MusicDataConfig.baseApiUrl}/artists/bind/new/LASTFM/${externalId}`,
        request
    );

    return createMasterEntityFromBinding(response.data, entityType);
}

/**
 * Unbinds an artist from LastFM
 * 
 * @param externalId The LastFM artist ID
 * @returns True if successful, false otherwise
 */
export async function unbindArtist(externalId: number): Promise<boolean> {
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/artists/unbind/LASTFM/${externalId}`
    );
    
    return response.data;
}
