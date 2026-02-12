import { MusicDataConfig } from '../config/musicdataconfig.ts';
import type { BaseMasterEntityDto } from "@/music/data/master/api/music-data-commons.ts";
import type { CategoryDto } from "@/music/data/master/api/music-data-categories.ts";
import { AlbumImpl } from "@/music/shared/types/entities.ts";
import type { BasePageSearchParams } from "@/music/shared/types/page.ts";

const masterDataApi = MusicDataConfig.api;

export interface AlbumDto extends BaseMasterEntityDto {
    primaryArtistId: number;
}

export interface AlbumWithCategoriesDto {
    id: number;
    name: string;
    primaryArtistId: number;
    categories: CategoryDto[];
}

export interface AlbumPageSearchParams extends BasePageSearchParams {}

export interface AlbumSaveRequest {
    id?: number;
    name: string;
    primaryArtistId?: number;
}

export interface AlbumCreateRequest {
    name: string;
    primaryArtistId: number;
}

export function createAlbumFromDto(dto: AlbumDto) {
    return new AlbumImpl(
        dto.id,
        dto.name,
        dto.primaryArtistId
    );
}

export function createAlbumWithCategoriesFromDto(dto: AlbumWithCategoriesDto) {
    const album = new AlbumImpl(dto.id, dto.name, dto.primaryArtistId);
    album.categories = dto.categories;
    return album;
}

/**
 * Creates a new album
 */
export async function createAlbum(album: AlbumCreateRequest) {
    const response = await masterDataApi.post<AlbumDto>(
        `/albums`,
        album
    );

    return createAlbumFromDto(response.data);
}

/**
 * Saves an existing album (PATCH-like: only sends changed fields)
 */
export async function saveAlbum(album: AlbumSaveRequest) {
    const response = await masterDataApi.post<AlbumDto>(
        `/albums`,
        album
    );

    return createAlbumFromDto(response.data);
}

/**
 * Deletes an album
 */
export async function deleteAlbum(albumId: number): Promise<boolean> {
    const response = await masterDataApi.delete<boolean>(
        `/albums/${albumId}`
    );

    return response.data;
}

/**
 * Fetches a single album with categories by ID
 */
export async function fetchAlbumWithCategories(albumId: number): Promise<AlbumWithCategoriesDto | null> {
    const response = await masterDataApi.get<AlbumWithCategoriesDto>(
        `/albums/${albumId}/with-categories`
    );

    return response.data;
}

/**
 * Binds album to category
 */
export async function bindAlbumToCategory(albumId: number, categoryId: number): Promise<void> {
    await masterDataApi.post(
        `/albums/${albumId}/categories/${categoryId}`
    );
}

/**
 * Unbinds album from category
 */
export async function unbindAlbumFromCategory(albumId: number, categoryId: number): Promise<void> {
    await masterDataApi.delete(
        `/albums/${albumId}/categories/${categoryId}`
    );
}
