import { MusicDataConfig } from '../config/musicdataconfig.ts';
import { ArtistImpl } from "@/music/shared/types/entities.ts";
import type { BaseMasterEntityDto } from "@/music/data/master/api/music-data-commons.ts";
import type { BasePageSearchParams } from "@/music/shared/types/page.ts";
import type { CategoryDto } from "./music-data-categories.ts";

const masterDataApi = MusicDataConfig.api;

export interface ArtistDto extends BaseMasterEntityDto {}

export interface ArtistWithCategoriesDto {
    id: number;
    name: string;
    categories: CategoryDto[];
}

export interface ArtistPageSearchParams extends BasePageSearchParams {
    categoryId?: number;
}

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
 * Fetches a single artist by ID
 */
export async function fetchArtist(artistId: number): Promise<ArtistDto> {
    const response = await masterDataApi.get<ArtistDto>(`/artists/${artistId}`);
    return response.data;
}

/**
 * Fetches a single artist with categories by ID
 */
export async function fetchArtistWithCategories(artistId: number): Promise<ArtistWithCategoriesDto | null> {
    const response = await masterDataApi.get<ArtistWithCategoriesDto>(
        `/artists/${artistId}/with-categories`
    );

    return response.data;
}

/**
 * Creates a new artist
 */
export async function createArtist(artist: ArtistCreateRequest) {
    const response = await masterDataApi.post<ArtistDto>(
        `/artists`,
        artist
    );

    return createArtistFromDto(response.data);
}

/**
 * Saves an existing artist
 */
export async function saveArtist(artist: ArtistSaveRequest) {
    const response = await masterDataApi.post<ArtistDto>(
        `/artists`,
        artist
    );

    return createArtistFromDto(response.data);
}

/**
 * Deletes an artist
 */
export async function deleteArtist(artistId: number): Promise<boolean> {
    const response = await masterDataApi.delete<boolean>(
        `/artists/${artistId}`
    );

    return response.data;
}

/**
 * Binds artist to category
 */
export async function bindArtistToCategory(artistId: number, categoryId: number): Promise<void> {
    await masterDataApi.post(
        `/artists/${artistId}/categories/${categoryId}`
    );
}

/**
 * Unbinds artist from category
 */
export async function unbindArtistFromCategory(artistId: number, categoryId: number): Promise<void> {
    await masterDataApi.delete(
        `/artists/${artistId}/categories/${categoryId}`
    );
}
