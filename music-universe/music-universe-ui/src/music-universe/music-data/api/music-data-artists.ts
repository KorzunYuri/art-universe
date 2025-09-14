import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import { ArtistImpl } from "@/music-universe/shared/types/entities.ts";
import type { BaseMasterEntityDto } from "@/music-universe/music-data/api/music-data-commons.ts";
import type { BasePageSearchParams } from "@/music-universe/shared/types/page.ts";

export interface ArtistDto extends BaseMasterEntityDto {}

export interface ArtistPageSearchParams extends BasePageSearchParams {}

export interface ArtistSaveRequest {
    id?: number;
    name: string;
}

export interface ArtistCreateRequest {
    name: string;
}

export function createArtistFromDto(dto: ArtistDto) {
    return new ArtistImpl(
        dto.id,
        dto.name
    );
}

/**
 * Creates a new artist
 * 
 * @param artist Artist data to create
 * @returns The created artist if successful, null otherwise
 */
export async function createArtist(artist: ArtistCreateRequest) {
    const response = await axios.post<ArtistDto>(
        `${MusicDataConfig.baseApiUrl}/artists`,
        artist
    );
    
    return createArtistFromDto(response.data);
}

/**
 * Saves an artist (create or update)
 * 
 * @param artist Artist data to save
 * @returns The saved artist if successful, null otherwise
 */
export async function saveArtist(artist: ArtistSaveRequest) {
    const response = await axios.post<ArtistDto>(
        `${MusicDataConfig.baseApiUrl}/artists`,
        artist
    );
    
    return createArtistFromDto(response.data);
}

/**
 * Deletes an artist
 * 
 * @param artistId The artist ID to delete
 * @returns True if successful, false otherwise
 */
export async function deleteArtist(artistId: number): Promise<boolean> {
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/artists/${artistId}`
    );
    
    return response.data;
}
