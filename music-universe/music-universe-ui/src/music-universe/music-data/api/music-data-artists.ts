import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import { ArtistImpl } from "@/music-universe/shared/types/entities.ts";
import type { BaseMasterEntityDto } from "@/music-universe/music-data/api/music-data-commons.ts";
import type { BasePageSearchParams } from "@/music-universe/shared/types/page.ts";
import type { CategoryDto } from "./music-data-categories.ts";

export interface ArtistDto extends BaseMasterEntityDto {}

export interface ArtistWithCategoriesDto {
    id: number;
    name: string;
    categories: CategoryDto[];
}

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

export function createArtistWithCategoriesFromDto(dto: ArtistWithCategoriesDto) {
    const artist = new ArtistImpl(dto.id, dto.name);
    artist.categories = dto.categories;
    return artist;
}

/**
 * Fetches a single artist with categories by ID
 */
export async function fetchArtistWithCategories(artistId: number): Promise<ArtistWithCategoriesDto | null> {
    const response = await axios.get<ArtistWithCategoriesDto>(
        `${MusicDataConfig.baseApiUrl}/artists/${artistId}/with-categories`
    );
    
    return response.data;
}

/**
 * Creates a new artist
 */
export async function createArtist(artist: ArtistCreateRequest) {
    const response = await axios.post<ArtistDto>(
        `${MusicDataConfig.baseApiUrl}/artists`,
        artist
    );
    
    return createArtistFromDto(response.data);
}

/**
 * Saves an existing artist
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
 */
export async function deleteArtist(artistId: number): Promise<boolean> {
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/artists/${artistId}`
    );
    
    return response.data;
}

/**
 * Binds artist to category
 */
export async function bindArtistToCategory(artistId: number, categoryId: number): Promise<void> {
    await axios.post(
        `${MusicDataConfig.baseApiUrl}/artists/${artistId}/categories/${categoryId}`
    );
}

/**
 * Unbinds artist from category
 */
export async function unbindArtistFromCategory(artistId: number, categoryId: number): Promise<void> {
    await axios.delete(
        `${MusicDataConfig.baseApiUrl}/artists/${artistId}/categories/${categoryId}`
    );
}
